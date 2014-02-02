package com.salvadordalvik.something.request;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.salvadordalvik.fastlibrary.util.FastUtils;
import com.salvadordalvik.something.list.PostItem;
import com.salvadordalvik.something.util.MustCache;
import com.salvadordalvik.something.util.SomePreferences;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 1/19/14.
 */
public class ThreadPageRequest extends HTMLRequest<ThreadPageRequest.ThreadPage> {

    public ThreadPageRequest(int threadId, int page, Response.Listener<ThreadPage> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/showthread.php", Request.Method.GET, success, error);
        addParam("threadid", threadId);
        if(page > 0){
            addParam("pagenumber", page);
        }else{
            addParam("goto", "newpost");
        }
    }

    @Override
    public ThreadPage parseHtmlResponse(NetworkResponse response, Document document) throws Exception {
        ArrayList<HashMap<String, String>> posts = new ArrayList<HashMap<String, String>>();

        int currentPage, maxPage = 1, threadId, forumId, unread = 0;

        unread = parsePosts(document, posts);

        Element pages = document.getElementsByClass("pages").first();
        currentPage = FastUtils.safeParseInt(pages.getElementsByAttribute("selected").attr("value"), 1);
        Element lastPage = pages.getElementsByTag("option").last();
        if(lastPage != null){
            maxPage = FastUtils.safeParseInt(lastPage.attr("value"), 1);
        }

        boolean bookmarked = document.getElementsByClass("unbookmark").size() > 0;

        String threadTitle = document.getElementsByClass("bclast").first().text();

        Element body = document.getElementsByTag("body").first();
        forumId = Integer.parseInt(body.attr("data-forum"));
        threadId = Integer.parseInt(body.attr("data-thread"));

        StringBuilder builder = new StringBuilder(2048);

        HashMap<String, String> headerArgs = new HashMap<String, String>();
        headerArgs.put("theme", getTheme(forumId));
        MustCache.applyHeaderTemplate(builder, headerArgs);

        for(HashMap<String, String> post : posts){
            MustCache.applyPostTemplate(builder, post);
        }

        if(currentPage == maxPage){
            builder.append("<div class='unread'></div>");
        }

        HashMap<String, String> footerArgs = new HashMap<String, String>();
        footerArgs.put("currentPage", Integer.toString(currentPage));
        footerArgs.put("pageCount", Integer.toString(maxPage));

        MustCache.applyFooterTemplate(builder, footerArgs);

        return new ThreadPage(builder.toString(), currentPage, maxPage, threadId, forumId, threadTitle, -unread, bookmarked);
    }

    public static String getTheme(int forumId){
        if(SomePreferences.forceTheme){
            return SomePreferences.selectedTheme;
        }else{
            switch (forumId){
                case 219:
                    return SomePreferences.amberYos ? "amberpos" : "yospos";
                case 26:
                    return "fyad";
                default:
                    return SomePreferences.selectedTheme;
            }
        }
    }

    public static class ThreadPage{
        public final int pageNum, maxPageNum, threadId, forumId, unreadDiff;
        public final String threadTitle, pageHtml;
        public final boolean bookmarked;

        private ThreadPage(String pageHtml, int pageNum, int maxPageNum, int threadId, int forumId, String threadTitle, int unreadDiff, boolean bookmarked){
            this.pageHtml = pageHtml;
            this.pageNum = pageNum;
            this.maxPageNum = maxPageNum;
            this.threadId = threadId;
            this.forumId = forumId;
            this.threadTitle = threadTitle;
            this.unreadDiff = unreadDiff;
            this.bookmarked = bookmarked;
        }
    }

    private static Pattern userJumpPattern = Pattern.compile("userid=(\\d+)");

    private static int parsePosts(Document doc, ArrayList<HashMap<String, String>> postArray){
        int unread = 0;
        Elements posts = doc.getElementsByClass("post");
        for(Element post : posts){
            String rawId = post.id().replaceAll("\\D", "");
            if(!TextUtils.isEmpty(rawId)){
                HashMap<String, String> postData = new HashMap<String, String>();
                postData.put("postID", rawId);
                String author = post.getElementsByClass("author").text();
                Element title = post.getElementsByClass("title").first();
                String avTitle = title.text();
                String avatarUrl = title.getElementsByTag("img").attr("src");
                String postContent = post.getElementsByClass("postbody").html();
                String postDate = post.getElementsByClass("postdate").text();

                Element userInfo = post.getElementsByClass("user_jump").first();
                Matcher userIdMatcher = userJumpPattern.matcher(userInfo.attr("href"));
                String userId = null;
                if(userIdMatcher.find()){
                    userId = userIdMatcher.group(1);
                }

                boolean previouslyRead = post.getElementsByClass("seen1").size() > 0 || post.getElementsByClass("seen2").size() > 0;
                if(!previouslyRead){
                    unread++;
                }

                postData.put("username", author);
                postData.put("avatarText", avTitle);
                postData.put("avatarURL", avatarUrl);
                postData.put("postcontent", postContent);
                postData.put("postDate", postDate);
                postData.put("userID", userId);
                postData.put("seen", previouslyRead ? "read" : "unread");

//                postData.put("regDate", post.getRegDate());
//                postData.put("lastReadUrl",  post.getLastReadUrl());
                //TODO handle image/avatar disable preference
//                postData.put("avatarURL", (aPrefs.canLoadAvatars() && post.getAvatar() != null &&  post.getAvatar().length()>0) ? post.getAvatar() : null);
                //TODO nullable, can wait to implement
//                postData.put("isOP", (aPrefs.highlightOP && post.isOp())?"op":null);
//                postData.put("isMarked", (aPrefs.markedUsers.contains(post.getUsername()))?"marked":null);
//                postData.put("isSelf", (aPrefs.highlightSelf && post.getUsername().equals(aPrefs.username)) ? "self" : null);
//                postData.put("notOnProbation", (aPrefs.isOnProbation())?null:"notOnProbation");
//                postData.put("editable", (post.isEditable())?"editable":null);
//                postData.put("mod", (post.isMod())?"mod":null);
//                postData.put("admin", (post.isAdmin())?"admin":null);

                postData.put("regDate", "");
                postData.put("lastReadUrl",  "");
                postData.put("isOP", null);
                postData.put("isMarked", null);
                postData.put("isSelf", null);
                postData.put("notOnProbation", "notOnProbation");
                postData.put("editable", null);
                postData.put("mod", null);
                postData.put("admin", null);

                postArray.add(postData);
            }
        }
        return unread;
    }
}

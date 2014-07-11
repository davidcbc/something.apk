package net.fastfourier.something.request;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;

import net.fastfourier.something.util.MustCache;
import net.fastfourier.something.util.SomePreferences;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by matthewshepard on 2/12/14.
 */
public class ProfileRequest extends HTMLRequest<ProfileRequest.ProfileData> {
    private int userid;

    public ProfileRequest(int userid, Response.Listener<ProfileData> success, Response.ErrorListener error) {
        super("http://forums.somethingawful.com/member.php", Request.Method.GET, success, error);
        addParam("action", "getinfo");
        addParam("userid", userid);
        this.userid = userid;
    }

    @Override
    public ProfileData parseHtmlResponse(Request<ProfileData> request, NetworkResponse response, Document document) throws Exception {

        StringBuilder profileHtml = new StringBuilder();
        parseProfile(document, profileHtml, userid);

        return new ProfileData(profileHtml.toString());
    }

    public static class ProfileData {
        public final String htmlData;

        public ProfileData(String profileData) {
            htmlData = profileData;
        }
    }
    private static void parseProfile(Document doc, StringBuilder html, int userid){
        HashMap<String, String> postData = new HashMap<String, String>();
        Element userInfo = doc.getElementsByClass("userinfo").first();
        if(userInfo != null) {

            String author = userInfo.getElementsByClass("author").text();
            Element title = userInfo.getElementsByClass("title").first();
            String avTitle = title.text();
            String avatarUrl = title.getElementsByTag("img").attr("src");
            String regDate = userInfo.getElementsByClass("registered").text();
            postData.put("username", author);
            postData.put("avatarText", avTitle);
            postData.put("avatarURL", avatarUrl);
            //Passing title through from fragment because parsing it out of the breadcrumb is a pita
            postData.put("userid", String.valueOf(userid));
        }else{
            throw new RuntimeException("Profile data not found!");
        }
        Element profileInfo = doc.getElementsByClass("info").first();
        if(profileInfo != null) {
            profileInfo.children().get(2);
            String additionalInfo = profileInfo.children().get(2).text();
            String numberOfPosts = profileInfo.children().get(1).getElementsByTag("b").get(0).text();

            Log.d("profile stuff",profileInfo.children().get(1).text());
            String profileLine = profileInfo.children().get(1).text();
            Pattern genderPattern = Pattern.compile(" to be a (\\S+).");
            Matcher genderMatcher = genderPattern.matcher(profileLine);
            String gender = "";
            if(genderMatcher.find()) {
                gender = genderMatcher.group(1);
            }
            Pattern postsPerDayPattern = Pattern.compile(" average of (\\d+[.]\\d+)");
            Matcher postsPerDayMatcher = postsPerDayPattern.matcher(profileLine);
            String postsPerDay = "";
            if(postsPerDayMatcher.find()) {
                postsPerDay = postsPerDayMatcher.group(1);
            }
            //Log.d("profile sex", sex);
            HashMap<String, String> headerArgs = new HashMap<String, String>();
            headerArgs.put("theme", SomePreferences.selectedTheme);
            headerArgs.put("jumpToPostId", "0");
            headerArgs.put("fontSize", SomePreferences.fontSize);
            headerArgs.put("previouslyRead", null);
            postData.put("seen", null);
            postData.put("additionalInfo", additionalInfo);
            postData.put("numberOfPosts", numberOfPosts);
            postData.put("gender",gender);
            postData.put("postsPerDay",postsPerDay);
            MustCache.applyHeaderTemplate(html, headerArgs);

            MustCache.applyProfileTemplate(html, postData);

            MustCache.applyFooterTemplate(html, null);
        }else{
            throw new RuntimeException("Profile data not found!");
        }
    }
}

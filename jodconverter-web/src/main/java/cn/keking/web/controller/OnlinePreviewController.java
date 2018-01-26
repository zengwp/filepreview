package cn.keking.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.keking.service.FileConverQueueTask;
import cn.keking.service.FilePreview;
import cn.keking.service.FilePreviewFactory;

/**
 * @author yudian-it
 */
@Controller
public class OnlinePreviewController {

    @Autowired
    FilePreviewFactory previewFactory;

    @Autowired
    RedissonClient redissonClient;

    /**
     * @param url
     * @param model
     * @return
     */
    @RequestMapping(value = "onlinePreview", method = RequestMethod.GET)
    public String onlinePreview(String url,String filePath, Model model, HttpServletRequest req) {
    	if(null==filePath || "".equals(filePath.trim()) || "undefined".equals(filePath.trim())){
    		filePath="otherTmp/";
    	}else {
    		filePath=filePath.trim()+"/";
    	}
    	url=Base64_decode(url);
        req.setAttribute("fileKey", req.getParameter("fileKey"));
        FilePreview filePreview = previewFactory.get(url);
        return filePreview.filePreviewHandle(url,filePath, model);
    }

    /**
     * 多图片切换预览
     *
     * @param model
     * @param req
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "picturesPreview", method = RequestMethod.GET)
    public String picturesPreview(String urls, Model model, HttpServletRequest req) throws UnsupportedEncodingException {
        // 路径转码
        String decodedUrl = URLDecoder.decode(urls, "utf-8");
        // 抽取文件并返回文件列表
        String[] imgs = decodedUrl.split("|");
        List imgurls = Arrays.asList(imgs);
        model.addAttribute("imgurls", imgurls);
        return "picture";
    }


    /**
     * 根据url获取文件内容
     * 当pdfjs读取存在跨域问题的文件时将通过此接口读取
     *
     * @param urlPath
     * @param resp
     */
    @RequestMapping(value = "/getCorsFile", method = RequestMethod.GET)
    public void getCorsFile(String urlPath, HttpServletResponse resp) {
        InputStream inputStream = null;
        try {
            String strUrl = urlPath.trim();
            URL url = new URL(strUrl);
            //打开请求连接
            URLConnection connection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            inputStream = httpURLConnection.getInputStream();
            byte[] bs = new byte[1024];
            int len;
            while (-1 != (len = inputStream.read(bs))) {
                resp.getOutputStream().write(bs, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }

    /**
     * 通过api接口入队
     * @param url 请编码后在入队
     */
    @GetMapping("/addTask")
    @ResponseBody
    public String addQueueTask(String url) {
        final RBlockingQueue<String> queue = redissonClient.getBlockingQueue(FileConverQueueTask.queueTaskName);
        queue.addAsync(url);
        return "success";
    }
    /**
     * Base64 解码
     */
    public static String Base64_decode(String str){
    	//处理空格
    	str=str.replace(" ","+");
    	try {
    		return new String(Base64.decodeBase64(str),"UTF-8");
    	} catch (UnsupportedEncodingException e) {
    		return "";
    	}
    }
    /**
     * Base64 加密
     */
    public static String Base64_encode(String str){
    	return Base64.encodeBase64String(str.getBytes());
    }
    public static void main(String[] args) {
    	//http://localhost:8012/onlinePreview?url=aHR0cDovL2xvY2FsaG9zdDo4MDEyL0Y6L2RlbW8vT25saW5lUHJldmlldy1tYXN0ZXIuemlwXy5fdG9tY2F0LXVzZXJzLnhtbA%3D%3D&fileKey=OnlinePreview-master.zip
		//System.out.println(Base64_encode("http://test-demo.gsucloud.com/files/itsm_files/itsm79/20180125/e21ed9e6-15b5-4d1c-95be-c3c508d52c60.doc"));
		System.out.println(Base64_decode("aHR0cDovL3Rlc3QtZGVtby5nc3VjbG91ZC5jb20vZmlsZXMvaXRzbV9maWxlcy9pdHNtNzkvMjAxODAxMjUvNmViYTVkNGItNmZkOC00MzU4LTk4ZGUtOWYxMzM1YThjNDI1LnppcCZ0aW1lPVJiN09YUzlNY0Q="));
	}
}

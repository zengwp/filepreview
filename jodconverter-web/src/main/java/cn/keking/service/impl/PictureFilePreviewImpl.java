package cn.keking.service.impl;

import cn.keking.service.FilePreview;
import cn.keking.utils.FileUtils;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.List;

/**
 * Created by kl on 2018/1/17.
 * Content :图片文件处理
 */
@Service
public class PictureFilePreviewImpl implements FilePreview {

    @Autowired
    FileUtils fileUtils;
    @Value("${file.dir}")
    String fileDir;

    @Override
    public String filePreviewHandle(String url,String dbPath, Model model) {
    	url=url.replace(fileDir,"");
        String fileKey=(String) RequestContextHolder.currentRequestAttributes().getAttribute("fileKey",0);
        List imgUrls = Lists.newArrayList(url);
        try{
            imgUrls.clear();
            imgUrls.addAll(fileUtils.getRedisImgUrls(fileKey));
        }catch (Exception e){
            imgUrls = Lists.newArrayList(url);
        }
        model.addAttribute("imgurls", imgUrls);
        model.addAttribute("currentUrl",imgUrls.get(0));
        return "picture";
    }
}

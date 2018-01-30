package cn.keking.service.impl;

import cn.keking.service.FilePreview;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

/**
 * Created by kl on 2018/1/17.
 * Content :处理pdf文件
 */
@Service
public class PdfFilePreviewImpl implements FilePreview{

    @Value("${file.dir}")
    String fileDir;
    
    @Override
    public String filePreviewHandle(String url,String dbPath, Model model) {
    	url=url.replace(fileDir,"");
        model.addAttribute("pdfUrl", url);
        return "pdf";
    }
}

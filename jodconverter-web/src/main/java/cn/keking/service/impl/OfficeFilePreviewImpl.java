package cn.keking.service.impl;

import cn.keking.model.FileAttribute;
import cn.keking.model.ReturnResponse;
import cn.keking.service.FilePreview;
import cn.keking.utils.DownloadUtils;
import cn.keking.utils.FileUtils;
import cn.keking.utils.OfficeToPdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * Created by kl on 2018/1/17.
 * Content :澶勭悊office鏂囦欢
 */
@Service
public class OfficeFilePreviewImpl implements FilePreview {

    @Autowired
    FileUtils fileUtils;

    @Value("${file.dir}")
    String fileDir;

    @Autowired
    DownloadUtils downloadUtils;

    @Autowired
    private OfficeToPdf officeToPdf;

    @Override
    public String filePreviewHandle(String url,String dbPath, Model model) {
        FileAttribute fileAttribute=fileUtils.getFileAttribute(url);
        String suffix=fileAttribute.getSuffix();
        String fileName=fileAttribute.getName();
        String decodedUrl=fileAttribute.getDecodedUrl();
        boolean isHtml = suffix.equalsIgnoreCase("xls") || suffix.equalsIgnoreCase("xlsx");
        String pdfName = fileName.substring(0, fileName.lastIndexOf(".") + 1) + (isHtml ? "html" : "pdf");
        // 鍒ゆ柇涔嬪墠鏄惁宸茶浆鎹㈣繃锛屽鏋滆浆鎹㈣繃锛岀洿鎺ヨ繑鍥烇紝鍚﹀垯鎵ц杞崲
        if (!fileUtils.listConvertedFiles().containsKey(pdfName)) {
            String filePath = fileDir + fileName;
            if (!new File(filePath).exists()) {
                ReturnResponse<String> response = downloadUtils.downLoad(decodedUrl, suffix, null,dbPath);
                if (0 != response.getCode()) {
                    model.addAttribute("msg", response.getMsg());
                    return "fileNotSupported";
                }
                filePath = response.getContent();
            }
            String outFilePath = fileDir+dbPath+dbPath + pdfName;
            File f=new File(fileDir+dbPath+dbPath);
            if(!f.exists()){
            	f.mkdir();
            }
            if (StringUtils.hasText(outFilePath)) {
                officeToPdf.openOfficeToPDF(filePath, outFilePath);
                /*File f = new File(filePath);
                if (f.exists()) {
                    f.delete();
                }*/
                if (isHtml) {
                    // 瀵硅浆鎹㈠悗鐨勬枃浠惰繘琛屾搷浣�(鏀瑰彉缂栫爜鏂瑰紡)
                    fileUtils.doActionConvertedFile(outFilePath);
                }
               // 鍔犲叆缂撳瓨
               fileUtils.addConvertedFile(pdfName, fileUtils.getRelativePath(outFilePath));
               
            }
        }
        model.addAttribute("pdfUrl", dbPath+dbPath+pdfName);
        return isHtml ? "html" : "pdf";
    }
}

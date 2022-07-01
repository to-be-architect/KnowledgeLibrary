package com.lib.web.user.main;

import com.lib.dao.UserInfoDao;
import com.lib.dto.FileInfoVO;
import com.lib.dto.FileNew;
import com.lib.dto.JSONResult;
import com.lib.dto.SerResult;
import com.lib.entity.FileInfo;
import com.lib.entity.RelationInfo;
import com.lib.entity.UserInfo;
import com.lib.enums.Const;
import com.lib.service.user.FileInfoService;
import com.lib.service.user.LuceneService;
import com.lib.utils.HtmlToWord;
import com.lib.utils.LuceneSearchUtil;
import com.lib.utils.StringValueUtil;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 新建文件的Controller
 *
 * @author Yu Yufeng
 */
@Controller
@RequestMapping("/user")
public class FileNewController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FileInfoService fileInfoService;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private LuceneService searchService;
    @Autowired
    private LuceneService lservice;

    @RequestMapping(value = "/file-content-search", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult searchFileContent(String searchInfo) {
        JSONResult<List<SerResult>> jr = null;
        List<SerResult> list = lservice.getParagraph(searchInfo, 20L);
        if (list.size() == 0) {
            jr = new JSONResult<List<SerResult>>(false, "<tr  colspan='3'><td  colspan='3'>没有找到相关内容</td></tr>");
        } else {
            jr = new JSONResult<List<SerResult>>(true, list);
        }
        return jr;
    }

    @RequestMapping(value = "/edit/{uuid}", method = RequestMethod.GET)
    public String editUI(Model model, @PathVariable("uuid") String uuid, HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        FileInfoVO fileInfo = fileInfoService.getFileInfoByUuid(uuid);
        model.addAttribute("fileInfo", fileInfo);
        return "file/edit";
    }

    @RequestMapping(value = "/newfile/complete", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult newFileComplete(String fileName, String content, HttpSession session) {
        FileNew fn = (FileNew) session.getAttribute(Const.SESSION_NEW_FILE);
        JSONResult jr = new JSONResult(true, "未知");
        if (fn == null) {
            jr = new JSONResult(false, "请先编辑保存");
            return jr;
        }
        String uuid = StringValueUtil.getUUID();
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        String path = Const.ROOT_PATH + "users/" + user.getUserId() + "/files/" + uuid + ".pdf";
        // System.out.println(path);
        // 判断用户有没有建文件夹
        File dir = new File(Const.ROOT_PATH + "users/" + user.getUserId() + "/files/");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            jr = new JSONResult(true, uuid);
            HtmlToWord.HtmlToPdf(fn.getContent(), path);
            //System.out.println(fn.getContent());
            File file = new File(path);
            FileInfo fi = new FileInfo();
            fileName = fn.getName();
            fi.setFileName(fileName);
            fi.setFileSize(file.length());
            fi.setFileExt("pdf");
            fi.setFileBrief(null);
            fi.setFileUserId(user.getUserId());
            fi.setFileUuid(uuid);
            fi.setFilePath("users/" + user.getUserId() + "/files/" + uuid);
            fi.setFileState(2);
            fi.setFileClassId(1l);
            fileInfoService.insertFile(fi);
            session.removeAttribute(Const.SESSION_NEW_FILE);

            // 处理文件
            try {
                fileInfoService.translateFile(uuid);
            } catch (Exception e) {
                LOG.error("文件{}处理失败", uuid, e);
            }

        } catch (Exception e) {
            LOG.error("", e);
            jr = new JSONResult(false, "转化失败");
        }
        return jr;
    }

    /**
     * 新建文档保存
     */
    @RequestMapping(value = "/newfile/save", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult newFileSave(String fileName, String content, HttpSession session) {
        JSONResult jr = new JSONResult(true, "暂存成功");
        if (null == fileName || fileName.equals("")) {
            fileName = "未知名" + new Date();
        }

        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        FileNew fn = (FileNew) session.getAttribute(Const.SESSION_NEW_FILE);
        if (null == fn) {
            fn = new FileNew();
        }
        fn.setName(fileName);
        fn.setContent(content);

        session.setAttribute(Const.SESSION_NEW_FILE, fn);
        return jr;
    }

    @RequestMapping(value = "/file-edit-submit", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult fileEditSave(FileInfo fileInfo, HttpSession session, Model model) {
        JSONResult jr = null;
        int res = fileInfoService.saveBaseFileInfoByUuid(fileInfo);
        FileInfo file = fileInfoService.getFileInfoByUuid(fileInfo.getFileUuid());
        if (res == 0) {
            jr = new JSONResult(false, "修改失败");
            return jr;
        } else if (res != 0 && fileInfo.getFileState() == null) {
            jr = new JSONResult(true, "修改成功");
            return jr;
        } else if (res != 0 && fileInfo.getFileState() == 5) {
            // 全文检索创立索引
            try {
                String fileText = LuceneSearchUtil.judge(file.getFileId());
                // System.out.println(fileText);
                searchService.deleteFileIndex(file);
                searchService.addFileIndex(file, userInfoDao.queryById(file.getFileUserId()).getUserName(), fileText);
            } catch (Exception e) {

            }
        } else {
            searchService.deleteFileIndex(file);
        }
        jr = new JSONResult(false, "修改成功");
        return jr;
    }

    @RequestMapping(value = "/file-search", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult<List<FileInfo>> searchByNameOrId(String searchInfo, HttpSession session,
                                                Integer pageNo) {
        if (pageNo == null) {
            pageNo = 1;
        }
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        JSONResult<List<FileInfo>> jr = null;
        List<FileInfo> list = fileInfoService.searchFileInfoByNameOrId(searchInfo, user.getUserId(), pageNo);
        jr = new JSONResult<List<FileInfo>>(true, list);
        return jr;
    }

    @RequestMapping(value = "/add-relations", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult<Integer> addRelations(@RequestBody JSONObject json, HttpSession session) {
        // System.out.println(obj);
        // JSONObject json = JSONObject.fromObject(obj);
        Long mainFileId = json.getLong("mainFileId");
        List<String> listStr = (List<String>) json.get("list");
        List<Long> list = new ArrayList<Long>();

        for (String l : listStr) {
            if (mainFileId != Long.valueOf(l) && !mainFileId.equals(l)) {
                list.add(Long.valueOf(l));
            }
        }

        int res = fileInfoService.addRelations(mainFileId, list);
        JSONResult<Integer> jr = null;
        jr = new JSONResult<Integer>(true, res);
        return jr;
    }

    /**
     * 自动关联
     */
    @RequestMapping(value = "/auto-relation/{uuid}", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult<Integer> autoRelations(@PathVariable("uuid") String uuid) {
        JSONResult<Integer> jr = null;
        if (uuid != null) {
            int res = fileInfoService.autoRelation(uuid);
            jr = new JSONResult<Integer>(true, res);
        } else {
            jr = new JSONResult<Integer>(false, 0);
        }

        return jr;
    }

    @RequestMapping(value = "/get-relations/{mainFileId}", method = RequestMethod.POST)
    public @ResponseBody
    JSONResult<List<RelationInfo>> getRelations(@PathVariable("mainFileId") Long mainFileId) {

        List<RelationInfo> res = fileInfoService.getRelations(mainFileId);
        JSONResult<List<RelationInfo>> jr = null;
        jr = new JSONResult<List<RelationInfo>>(true, res);
        return jr;
    }

    @RequestMapping(value = "/del-relations/{mainFileId}/{relationFileId}", method = RequestMethod.DELETE)
    public @ResponseBody
    JSONResult<Integer> delRelations(@PathVariable("mainFileId") Long mainFileId,
                                     @PathVariable("relationFileId") Long relationFileId) {
        int res = fileInfoService.delRelations(mainFileId, relationFileId);
        JSONResult<Integer> jr = null;
        jr = new JSONResult<Integer>(true, res);
        return jr;
    }

}

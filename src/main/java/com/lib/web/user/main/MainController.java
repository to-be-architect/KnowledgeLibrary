package com.lib.web.user.main;

import com.github.pagehelper.PageInfo;
import com.lib.dto.FileInfoVO;
import com.lib.dto.JSONResult;
import com.lib.entity.Classification;
import com.lib.entity.FileInfo;
import com.lib.entity.UserInfo;
import com.lib.enums.Const;
import com.lib.service.admin.CountService;
import com.lib.service.user.AdminCountService;
import com.lib.service.user.FileManageService;
import com.lib.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URL;
import java.util.List;

/**
 * 主要页面跳转
 *
 * @author Yu Yufeng
 */
@Controller
@RequestMapping("/user")
public class MainController {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private FileManageService fileManageService;
    @Autowired
    private UserService userService;
    @Autowired
    private AdminCountService countService;
    @Autowired
    private CountService ctService;

    public static void main(String[] args) throws Exception {
        String url = "http://yyf.tunnel.qydev.com/lib/user/index";
        URL u = new URL(url);

    }

    /**
     * 获取今日的录入文件数量
     *
     * @return
     */
    @RequestMapping(value = "/count-today", method = RequestMethod.GET)
    public @ResponseBody
    JSONResult<Long> getTodaysUpload() {
        Long count = countService.getTodaysUpload();
        JSONResult<Long> jr = new JSONResult<Long>(true, count);
        return jr;
    }

    /**
     * 获取用户上传的资源数量
     *
     * @return
     */
    @RequestMapping(value = "/count-userfiles", method = RequestMethod.GET)
    public @ResponseBody
    JSONResult<Long> getCountUserFiles(HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        Long count = countService.getCountUserFiles(user.getUserId());
        JSONResult<Long> jr = new JSONResult<Long>(true, count);
        return jr;
    }

    /**
     * 获取用户可用资源数量
     *
     * @return
     */
    @RequestMapping(value = "/count-publicfiles", method = RequestMethod.GET)
    public @ResponseBody
    JSONResult<Long> getCountPublicFiles(HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        Long count = countService.getCountPublicFiles(user.getUserId());
        JSONResult<Long> jr = new JSONResult<Long>(true, count);
        return jr;
    }

    /**
     * 获取用户的收藏数量
     *
     * @return
     */
    @RequestMapping(value = "/count-forkfiles", method = RequestMethod.GET)
    public @ResponseBody
    JSONResult<Long> getCountForkFiles(HttpSession session) {
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        Long count = countService.getCountForkFiles(user.getUserId());
        JSONResult<Long> jr = new JSONResult<Long>(true, count);
        return jr;
    }

    /**
     * 跳转到主页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String login(Model model, HttpSession session, HttpServletRequest req) {

        try {
            String url = req.getRemoteAddr();
            String reqUrl = req.getRequestURL().toString();
            LOG.info(reqUrl + " from " + url);
            UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
            // 获取最近浏览记录
            List<FileInfoVO> recent = fileManageService.getRecenREeadtFile(user.getUserId());
            model.addAttribute("recent", recent);

            // 获取最近分享
            List<FileInfoVO> share = fileManageService.getRecenShareFile(user.getUserId());
            model.addAttribute("share", share);

            // 获取最近热门
            List<FileInfoVO> hot = fileManageService.getRecentHotFile();
            model.addAttribute("hot", hot);
        } catch (Exception e) {
            LOG.info("login user/index", e);
        }


        //获取推荐文档
//		List<FileInfo> recommed = ctService.getFileScoreList(user.getUserId(), 5);
//		List<FileInfo> recommed = ctService.getFileScoreListByItemCF(user.getUserId(), 5);
//		List<FileInfo> recommed = ctService.getFileScoreListBySlopOne(user.getUserId(), 5);
//		model.addAttribute("recommed", recommed);


        return "main/index";
    }

    /**
     * 文档推荐
     */
    @ResponseBody
    @RequestMapping(value = "/recommend", method = {RequestMethod.POST, RequestMethod.GET})
    public JSONResult<List<FileInfo>> recommend(HttpSession session) {
        JSONResult<List<FileInfo>> jr = null;
        UserInfo user = (UserInfo) session.getAttribute(Const.SESSION_USER);
        int recomNum = 6;
        try {
            //获取推荐文档
            List<FileInfo> recommed = ctService.getFileScoreList(user.getUserId(), recomNum);
            //	List<FileInfo> recommed = ctService.getFileScoreListByItemCF(user.getUserId(), 5);
            //	List<FileInfo> recommed = ctService.getFileScoreListBySlopOne(user.getUserId(), 5);
            jr = new JSONResult<List<FileInfo>>(true, recommed);
        } catch (Exception e) {
            jr = new JSONResult<List<FileInfo>>(false, "获取失败");
        }
        return jr;
    }

    @RequestMapping(value = "/public/{fileClassId}/{pageNo}", method = RequestMethod.GET)
    public String publicResource(Model model, @PathVariable("fileClassId") Long fileClassId,
                                 @PathVariable("pageNo") Integer pageNo) {

        try {
            List<Classification> list = fileManageService.getClassificationByParentId(fileClassId);
            // 得到所有父节点链表
            List<Classification> plist = fileManageService.getFatherClassesById(fileClassId);
            Classification c = fileManageService.getClassificationById(fileClassId);
            // list.add(0, c);
            PageInfo<FileInfoVO> page = fileManageService.getAllChildFiles(pageNo, fileClassId);
            model.addAttribute("classi", c);
            model.addAttribute("list", list);
            model.addAttribute("plist", plist);

            model.addAttribute("page", page);
        } catch (Exception e) {
            LOG.error("publicResource fileClassId=" + fileClassId + ",pageNo=" + pageNo, e);
        }

        return "main/public";
    }
}

package com.shopping.view.app.action;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hibernate.Session;
import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.shopping.core.annotation.SecurityMapping;
import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.security.support.SecurityUserHolder;
import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.core.tools.WebForm;
import com.shopping.foundation.domain.Accessory;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.domain.MobileVerifyCode;
import com.shopping.foundation.domain.SnsFriend;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.query.SnsFriendQueryObject;
import com.shopping.foundation.domain.query.UserQueryObject;
import com.shopping.foundation.service.IAccessoryService;
import com.shopping.foundation.service.IAreaService;
import com.shopping.foundation.service.IMobileVerifyCodeService;
import com.shopping.foundation.service.ISnsFriendService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.ITemplateService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.manage.admin.tools.MsgTools;
import com.shopping.uc.api.UCClient;
import com.shopping.view.web.tools.AreaViewTools;

@Controller
public class ApiAccountBuyerAction {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IMobileVerifyCodeService mobileverifycodeService;

    @Autowired
    private IAreaService areaService;
    
    @Autowired
    private IUserConfigService userConfigService;

    @Autowired
    private ISnsFriendService sndFriendService;

    @Autowired
    private MsgTools msgTools;
    private static final String DEFAULT_AVATAR_FILE_EXT = ".jpg";
    //private static BASE64Decoder _decoder = new BASE64Decoder();
    public static final String OPERATE_RESULT_CODE_SUCCESS = "200";
    public static final String OPERATE_RESULT_CODE_FAIL = "400";


    @RequestMapping(value = "/api/load_area.htm")
    @ResponseBody
    public String getLoadArea(HttpServletRequest request, HttpServletResponse response, String pid){
        List list = new ArrayList();
        String sql="";
        String content;
        if((pid != null) && (!pid.equals(""))){
            Map params = new HashMap();
            params.put("pid", Long.valueOf(Long.parseLong(pid)));
            sql = "select obj from Area obj where obj.parent.id=:pid";
            List<Area> areas = this.areaService.query(
                    "select obj from Area obj where obj.parent.id=:pid", params,
                    -1, -1);

            for (Area area : areas) {
                Map map = new HashMap();
                map.put("id", area.getId());
                map.put("areaName", area.getAreaName());
                list.add(map);
            }
            content = Json.toJson(list, JsonFormat.compact());
            System.out.println(content);

        }else{
            List areas = this.areaService.query(
                    "select obj from Area obj where obj.parent.id is null", null,
                    -1, -1);
            content = Json.toJson(areas, JsonFormat.compact());
        }
        return "{\"statusCode\":200,\"data\":"+content+"}";
    }


    /**
     * 个人信息保存
     *
     * @param request
     * @param response
     * @param area_id  区域id
     * @return
     */
    @RequestMapping(value = {"/api/account_save.htm"}, produces = {"application/json;charset=utf-8"})
    @ResponseBody
    public String account_save(HttpServletRequest request, HttpServletResponse response, String area_id, User user) {
        System.out.println(user);
        if ((area_id != null) && (!area_id.equals(""))) {
            Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
            user.setArea(area);
        }
//        if ((birthday != null) && (!birthday.equals(""))) {
//            String[] y = birthday.split("-");
//            Calendar calendar = new GregorianCalendar();
//            int years = calendar.get(1) - CommUtil.null2Int(y[0]);
//            user.setYears(years);
//        }
        user.setArea(this.areaService.getObjById(CommUtil.null2Long(area_id)));
        this.userService.update(user);
        return "{\"statusCode\":200,\"msg\":保存个人信息成功！}";
//	        mv.addObject("op_title", "个人信息修改成功");
//	        mv.addObject("url", CommUtil.getURL(request) + "/buyer/account.htm");
//	        return mv;
    }

    /**
     * 密码修改保存
     *
     * @param request
     * @param response
     * @param old_password 旧密码
     * @param new_password 新密码
     * @return
     * @throws Exception
     */
//	    @SecurityMapping(display = false, rsequence = 0, title = "密码修改保存", value = "/buyer/account_password_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping(value = {"/api/account_password_save.htm"})
    @ResponseBody
    public String account_password_save(HttpServletRequest request, HttpServletResponse response, String old_password, String new_password) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user.getPassword().equals(
                Md5Encrypt.md5(old_password).toLowerCase())) {
            user.setPassword(Md5Encrypt.md5(new_password).toLowerCase());
            boolean ret = this.userService.update(user);
            if ((ret) && (this.configService.getSysConfig().isUc_bbs())) {
                UCClient uc = new UCClient();
                String str = uc.uc_user_edit(user.getUsername(),
                        CommUtil.null2String(old_password),
                        CommUtil.null2String(new_password),
                        CommUtil.null2String(user.getEmail()), 1, 0, 0);
            }
            return "{\"statusCode\":200,\"msg\":\"修改密码成功！\"}";
//	            mv.addObject("op_title", "密码修改成功");
//	            send_sms(request, "sms_tobuyer_pws_modify_notify");
        } else {
//	            mv = new JModelAndView("error.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response);
//	            if ((shopping_view_type != null) && (!shopping_view_type.equals("")) && (shopping_view_type.equals("wap"))) {
//	                mv = new JModelAndView("wap/error.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response);
//	            }
//	            mv.addObject("op_title", "原始密码输入错误，修改失败");
            return "{\"statusCode\":400,\"msg\":\"原始密码输入错误，修改失败!\"}";
        }
//	        mv.addObject("url", CommUtil.getURL(request) + "/buyer/account_password.htm");
//	        return mv;
    }

    /**
     * 邮箱修改保存
     *
     * @param request
     * @param response
     * @param password 密码
     * @param email    新邮箱
     * @return
     */
//	    @SecurityMapping(display = false, rsequence = 0, title = "邮箱修改保存", value = "/buyer/account_email_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping(value = {"/api/account_email_save.htm"})
    @ResponseBody
    public String account_email_save(HttpServletRequest request, HttpServletResponse response, String password, String email) {
//	        ModelAndView mv = new JModelAndView("success.html", this.configService
//	                .getSysConfig(), this.userConfigService.getUserConfig(), 1,
//	                request, response);
//	        WebForm wf = new WebForm();
        User user = this.userService.getObjById(
                SecurityUserHolder.getCurrentUser().getId());
        if (user.getPassword().equals(Md5Encrypt.md5(password).toLowerCase())) {
            user.setEmail(email);
            this.userService.update(user);
            return "{\"statusCode\":200,\"msg\":邮箱修改成功！}";
//	            mv.addObject("op_title", "邮箱修改成功");
        } else {
//	            mv = new JModelAndView("error.html", this.configService.getSysConfig(),
//	                    this.userConfigService.getUserConfig(), 1, request,
//	                    response);
//	            mv.addObject("op_title", "密码输入错误，邮箱修改失败");
            return "{\"statusCode\":400,\"msg\":密码输入错误,修改邮箱失败！}";
        }
//	        mv.addObject("url", CommUtil.getURL(request) +
//	                "/buyer/account_email.htm");
//	        return mv;
    }

    /**
     * 手机号码修改
     *
     * @param request
     * @param response
     * @param mobile_verify_code 手机短信验证码
     * @param mobile             手机号
     * @return
     * @throws Exception
     */
//	    @SecurityMapping(display = false, rsequence = 0, title = "手机号码保存", value = "/buyer/account_mobile_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping(value = {"/api/account_mobile_save.htm"})
    @ResponseBody
    public String account_mobile_save(HttpServletRequest request, HttpServletResponse response, String mobile_verify_code, String mobile) throws Exception {
//	        ModelAndView mv = new JModelAndView("success.html", this.configService
//	                .getSysConfig(), this.userConfigService.getUserConfig(), 1,
//	                request, response);
//	        WebForm wf = new WebForm();
        User user = this.userService.getObjById(
                SecurityUserHolder.getCurrentUser().getId());
        MobileVerifyCode mvc = this.mobileverifycodeService.getObjByProperty(
                "mobile", mobile);
        if ((mvc != null) && (mvc.getCode().equalsIgnoreCase(mobile_verify_code))) {
            user.setMobile(mobile);
            this.userService.update(user);
            this.mobileverifycodeService.delete(mvc.getId());
//	            mv.addObject("op_title", "手机绑定成功");

//            send_sms(request, "sms_tobuyer_mobilebind_notify");
            return "{\"statusCode\":200,\"msg\":手机绑定成功！}";
//	            mv
//	                    .addObject("url", CommUtil.getURL(request) +
//	                            "/buyer/account.htm");
        } else {
//	            mv = new JModelAndView("error.html", this.configService.getSysConfig(),
//	                    this.userConfigService.getUserConfig(), 1, request,
//	                    response);
//	            mv.addObject("op_title", "验证码错误，手机绑定失败");
//	            mv.addObject("url", CommUtil.getURL(request) +
//	                    "/buyer/account_mobile.htm");
            return "{\"statusCode\":400,\"msg\":验证码错误，手机绑定失败}";
        }
//	        return mv;
    }

    /**
     * 手机短信验证码发送
     *
     * @param request
     * @param response
     * @param type
     * @param mobile   手机号
     * @throws UnsupportedEncodingException
     */
//	    @SecurityMapping(display = false, rsequence = 0, title = "手机短信发送", value = "/buyer/account_mobile_sms.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping(value = {"/api/account_mobile_sms.htm"})
    @ResponseBody
    public void account_mobile_sms(HttpServletRequest request, HttpServletResponse response, String type, String mobile)
            throws UnsupportedEncodingException {
        String ret = "100";
        if (type.equals("mobile_vetify_code")) {
            String code = CommUtil.randomString(4).toUpperCase();
            String content = "尊敬的" +
                    SecurityUserHolder.getCurrentUser().getUserName() +
                    "您好，您在试图修改" +
                    this.configService.getSysConfig().getWebsiteName() +
                    "用户绑定手机，手机验证码为：" + code + "。[" +
                    this.configService.getSysConfig().getTitle() + "]";
            if (this.configService.getSysConfig().isSmsEnbale()) {
                boolean ret1 = this.msgTools.sendSMS(mobile, content);
                if (ret1) {
                    MobileVerifyCode mvc = this.mobileverifycodeService
                            .getObjByProperty("mobile", mobile);
                    if (mvc == null) {
                        mvc = new MobileVerifyCode();
                    }
                    mvc.setAddTime(new Date());
                    mvc.setCode(code);
                    mvc.setMobile(mobile);
                    this.mobileverifycodeService.update(mvc);
                } else {
                    ret = "200";
                }
            } else {
                ret = "300";
            }
            response.setContentType("text/plain");
            response.setHeader("Cache-Control", "no-cache");
            response.setCharacterEncoding("UTF-8");
            try {
                PrintWriter writer = response.getWriter();
                writer.print(ret);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 图像上传
     * @param request
     * @param response
     * @throws IOException
     */
//    @SecurityMapping(display = false, rsequence = 0, title = "图像上传", value = "/buyer/upload_avatar.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping({"/api/upload_avatar.htm"})
    @ResponseBody
    public String upload_avatar(HttpServletRequest request, HttpServletResponse response) throws IOException {
//        response.setContentType("text/html;charset=UTF-8");
//        response.setHeader("Pragma", "No-cache");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setDateHeader("Expires", 0L);
        try {
            String filePath = request.getSession().getServletContext().getRealPath("") + "/upload/avatar";
            File uploadDir = new File(filePath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            String customParams = CommUtil.null2String(request.getParameter("custom_params"));
            String imageType = CommUtil.null2String(request.getParameter("image_type"));
            if ("".equals(imageType)) {
                imageType = ".jpg";
            }

            String bigAvatarContent = CommUtil.null2String(request.getParameter("big_avatar"));
            User user = SecurityUserHolder.getCurrentUser();
            String bigAvatarName = SecurityUserHolder.getCurrentUser().getId() + "_big";

            saveImage(filePath, imageType, bigAvatarContent, bigAvatarName);
            Accessory photo = new Accessory();
            if (user.getPhoto() != null) {
                photo = user.getPhoto();
            } else {
                photo.setAddTime(new Date());
                photo.setWidth(132);
                photo.setHeight(132);
            }
            photo.setName(bigAvatarName + imageType);
            photo.setExt(imageType);
            photo.setPath(this.configService.getSysConfig().getUploadFilePath() + "/avatar");
//            if (user.getPhoto() == null)
//                this.accessoryService.save(photo);
//            else {
//                this.accessoryService.update(photo);
//            }
            user.setPhoto(photo);
            this.userService.update(user);
            return "{\"statusCode\":200,\"msg\":\"上传成功\"}";
//            response.setContentType("text/xml");
//
//            response.getWriter().write("200");
        } catch (Exception e) {
            e.printStackTrace();
//            response.setContentType("text/xml");
//
//            response.getWriter().write("400");
            return "{\"statusCode\":400,\"msg\":\"上传失败！\"}";
        }
    }

    private void saveImage(String filePath, String imageType, String avatarContent, String avatarName)
            throws IOException {
        avatarContent = CommUtil.null2String(avatarContent);
        if (!"".equals(avatarContent)) {
            if ("".equals(avatarName))
                avatarName = UUID.randomUUID().toString() +
                        ".jpg";
            else {
                avatarName = avatarName + imageType;
            }
            //byte[] data = _decoder.decodeBuffer(avatarContent);
            byte[] data = Base64.decodeBase64(avatarContent);
            File f = new File(filePath + File.separator + avatarName);
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(f));
            dos.write(data);
            dos.flush();
            dos.close();
        }
    }

//    private void send_sms(HttpServletRequest request, String mark) throws Exception {
//        com.shopping.foundation.domain.Template template = this.templateService.getObjByProperty("mark", mark);
//        if ((template != null) && (template.isOpen())) {
//            User user = this.userService.getObjById(
//                    SecurityUserHolder.getCurrentUser().getId());
//            String mobile = user.getMobile();
//            if ((mobile != null) && (!mobile.equals(""))) {
//                String path = request.getSession().getServletContext()
//                        .getRealPath("/") +
//                        "/vm/";
//                PrintWriter pwrite = new PrintWriter(
//                        new OutputStreamWriter(new FileOutputStream(path + "msg.vm", false), "UTF-8"));
//                pwrite.print(template.getContent());
//                pwrite.flush();
//                pwrite.close();
//
//                Properties p = new Properties();
//                p.setProperty("file.resource.loader.path", request
//                        .getRealPath("/") +
//                        "vm" + File.separator);
//                p.setProperty("input.encoding", "UTF-8");
//                p.setProperty("output.encoding", "UTF-8");
//                Velocity.init(p);
//                org.apache.velocity.Template blank = Velocity.getTemplate(
//                        "msg.vm", "UTF-8");
//                VelocityContext context = new VelocityContext();
//                context.put("user", user);
//                context.put("config", this.configService.getSysConfig());
//                context.put("send_time", CommUtil.formatLongDate(new Date()));
//                context.put("webPath", CommUtil.getURL(request));
//                StringWriter writer = new StringWriter();
//                blank.merge(context, writer);
//
//                String content = writer.toString();
//                this.msgTools.sendSMS(mobile, content);
//            }
//        }
//    }
    
    
    /**
     * 好友管理显示好友列表
     * @param request
     * @param response
     * @param currentPage
     * @return
     */
//    @SecurityMapping(display = false, rsequence = 0, title = "好友管理", value = "/api/friend.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping({"/api/friend.htm"})
    @ResponseBody
    public String account_friend(HttpServletRequest request, HttpServletResponse response, String currentPage) {
        ModelAndView mv = new JModelAndView(
                "user/default/usercenter/account_friend.html", this.configService
                .getSysConfig(),
                this.userConfigService.getUserConfig(), 0, request, response);
        SnsFriendQueryObject qo = new SnsFriendQueryObject(currentPage, mv,
                "addTime", "desc");
        qo.addQuery("obj.fromUser.id",
                new SysMap("user_id",
                        SecurityUserHolder.getCurrentUser().getId()), "=");
        IPageList pList = this.sndFriendService.list(qo);
        List<SnsFriend> list=pList.getResult();
        List<User> users=new ArrayList<User>();
        for(SnsFriend sf : list){
        	users.add(sf.getToUser());
        }
        String temp = JSON.toJSONString(users);
        System.out.println(users);

        return "{\"statusCode\":200,\"msg\":"+temp+"}";
        
//        CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
        
    }
    
    
    /**
     * 搜索用户
     * @param request
     * @param response
     * @param userName
     * @param area_id
     * @param sex
     * @param years
     * @param currentPage
     * @return
     */
//    @SecurityMapping(display = false, rsequence = 0, title = "搜索用户", value = "/buyer/account_friend_search.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping({"/api/account_friend_search.htm"})
    @ResponseBody
    public String friend_search(HttpServletRequest request, HttpServletResponse response, String userName, String area_id, String sex, String years, String currentPage) {
        ModelAndView mv = new JModelAndView(
                "user/default/usercenter/account_friend_search.html",
                this.configService.getSysConfig(), this.userConfigService
                .getUserConfig(), 0, request, response);
        UserQueryObject qo = new UserQueryObject(currentPage, mv, "addTime",
                "desc");
        qo.addQuery("obj.userRole", new SysMap("userRole", "ADMIN"), "!=");
        if ((userName != null) && (!userName.equals(""))) {
            mv.addObject("userName", userName);
            qo.addQuery("obj.userName",
                    new SysMap("userName", "%" + userName +
                            "%"), "like");
        }
        if ((years != null) && (!years.equals(""))) {
            mv.addObject("years", years);
            if (years.equals("18")) {
                qo.addQuery("obj.years",
                        new SysMap("years",
                                Integer.valueOf(CommUtil.null2Int(years))), "<=");
            }
            if (years.equals("50")) {
                qo.addQuery("obj.years",
                        new SysMap("years",
                                Integer.valueOf(CommUtil.null2Int(years))), ">=");
            }
            if ((!years.equals("18")) && (!years.equals("50"))) {
                String[] y = years.split("~");
                qo.addQuery("obj.years",
                        new SysMap("years",
                                Integer.valueOf(CommUtil.null2Int(y[0]))), ">=");
                qo.addQuery("obj.years",
                        new SysMap("years2",
                                Integer.valueOf(CommUtil.null2Int(y[1]))), "<=");
            }
        }
        if ((sex != null) && (!sex.equals(""))) {
            mv.addObject("sex", sex);
            qo.addQuery("obj.sex", new SysMap("sex", Integer.valueOf(CommUtil.null2Int(sex))),
                    "=");
        }
        if ((area_id != null) && (!area_id.equals(""))) {
            Area area = this.areaService
                    .getObjById(CommUtil.null2Long(area_id));
            mv.addObject("area", area);
            qo.addQuery("obj.area.id",
                    new SysMap("area_id",
                            CommUtil.null2Long(area_id)), "=");
        }
        qo.setPageSize(Integer.valueOf(18));
        qo.addQuery("obj.id",
                new SysMap("user_id",
                        SecurityUserHolder.getCurrentUser().getId()), "!=");
        IPageList pList = this.userService.list(qo);
//        CommUtil.saveIPageList2ModelAndView("", "", "", pList, mv);
//        List areas = this.areaService.query(
//                "select obj from Area obj where obj.parent.id is null", null,
//                -1, -1);
//        mv.addObject("areas", areas);
        List<User> users=pList.getResult();
        String data=JSON.toJSONString(users);
        System.out.println(pList.getResult());
//        return mv;
        return "{\"statusCode\":200,\"msg\":"+data+"}";
    }
    
    
    /**
     * 添加好友
     * @param request
     * @param response
     * @param user_id 用户id
     * @return
     */
//    @SecurityMapping(display = false, rsequence = 0, title = "好友添加", value = "/buyer/friend_add_save.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping({"/api/friend_add_save.htm"})
    @ResponseBody
    public String friend_add_save(HttpServletRequest request, HttpServletResponse response, String user_id) {
        boolean flag = false;
        Map params = new HashMap();
        params.put("user_id", CommUtil.null2Long(user_id));
        params.put("uid", SecurityUserHolder.getCurrentUser().getId());
        List sfs = this.sndFriendService
                .query(
                        "select obj from SnsFriend obj where obj.fromUser.id=:uid and obj.toUser.id=:user_id",
                        params, -1, -1);
        if (sfs.size() == 0) {
            SnsFriend friend = new SnsFriend();
            friend.setAddTime(new Date());
            friend.setFromUser(SecurityUserHolder.getCurrentUser());
            friend.setToUser(this.userService.getObjById(
                    CommUtil.null2Long(user_id)));
            flag = this.sndFriendService.save(friend);
            if(flag==true){
           	 return "{\"statusCode\":200,\"msg\":\"添加好友成功！\"}";
           }else{
           	 return "{\"statusCode\":400,\"msg\":\"添加好友失败！\"}";
           }
        }else{
        	 return "{\"statusCode\":401,\"msg\":\"您已添加该好友！\"}";
        }
//        response.setContentType("text/plain");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setCharacterEncoding("UTF-8");
//        try {
//            PrintWriter writer = response.getWriter();
//            writer.print(flag);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        
    }
    
    /**
     * 删除好友
     * @param request
     * @param response
     * @param id 需要删除的好友用户id
     */
//    @SecurityMapping(display = false, rsequence = 0, title = "好友删除", value = "/buyer/friend_del.htm*", rtype = "buyer", rname = "用户中心", rcode = "user_center", rgroup = "用户中心")
    @RequestMapping({"/api/friend_del.htm"})
    @ResponseBody
    public String friend_del(HttpServletRequest request, HttpServletResponse response, String id) {
    	boolean flag = this.sndFriendService.delete(CommUtil.null2Long(id));
    	if(flag==true){
    		return "{\"statusCode\":200,\"msg\":\"删除成功！\"}";
    	}else{
    		return "{\"statusCode\":400,\"msg\":\"删除失败\"}";
    	}
//        response.setContentType("text/plain");
//        response.setHeader("Cache-Control", "no-cache");
//        response.setCharacterEncoding("UTF-8");
//        try {
//            PrintWriter writer = response.getWriter();
//            writer.print(true);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
    

}

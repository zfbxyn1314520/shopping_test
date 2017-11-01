package com.shopping.view.app.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserService;
import com.shopping.manage.admin.tools.MsgTools;

@Controller
public class ApiIndexViewAction {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IUserService userService;

    @Autowired
    private MsgTools msgTools;


    /**
     * 发送新密码到绑定邮箱去
     *
     * @param request
     * @param response
     * @param userName 输入的用户名
     * @param email    邮箱
     * @param code     验证码
     * @return
     */
    @RequestMapping({"/api/find_pws.htm"})
    @ResponseBody
    public String find_pws(HttpServletRequest request, HttpServletResponse response, String userName, String email, String code) {
//		ModelAndView mv = new JModelAndView( "success.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response );
        HttpSession session = request.getSession(false);
        String verify_code = (String) session.getAttribute("verify_code");
        if (code.toUpperCase().equals(verify_code)) {
            User user = this.userService.getObjByProperty("userName", userName);
            if (user.getEmail().equals(email.trim())) {
                String pws = CommUtil.randomString(6).toLowerCase();
                String subject = this.configService.getSysConfig().getTitle() + "密码找回邮件";
                String content = user.getUsername() + ",您好！您通过密码找回功能重置密码，新密码为：" + pws;
                boolean ret = this.msgTools.sendEmail(email, subject, content);
                if (ret) {
                    user.setPassword(Md5Encrypt.md5(pws));
                    this.userService.update(user);
//					mv.addObject( "op_title", "新密码已经发送到邮箱:<font color=red>" + email + "</font>，请查收后重新登录" );
//					mv.addObject( "url", CommUtil.getURL( request ) + "/user/login.htm" );
                    return "{\"statusCode\":200,\"msg\":\"新密码已经发送到您的邮箱！请查收后重新登录\"}";
                } else {
//					mv = new JModelAndView( "error.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response );
//					mv.addObject( "op_title", "邮件发送失败，密码暂未执行重置" );
//					mv.addObject( "url", CommUtil.getURL( request ) + "/forget.htm" );
                    return "{\"statusCode\":400,\"msg\":\"邮件发送失败，密码暂未执行重置\"}";
                }
            } else {
//				mv = new JModelAndView( "error.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response );
//				mv.addObject( "op_title", "用户名、邮箱不匹配" );
//				mv.addObject( "url", CommUtil.getURL( request ) + "/forget.htm" );
                return "{\"statusCode\":401,\"msg\":\"用户名、邮箱不匹配\"}";
            }
        } else {
////			mv = new JModelAndView( "error.html", this.configService.getSysConfig(), this.userConfigService.getUserConfig(), 1, request, response );
////			mv.addObject( "op_title", "验证码不正确" );
////			mv.addObject( "url", CommUtil.getURL( request ) + "/forget.htm" );
            return "{\"statusCode\":402,\"msg\":\"验证码不正确\"}";
        }
//		return mv;
    }
}

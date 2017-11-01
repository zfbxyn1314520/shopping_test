package com.shopping.view.app.action;

import com.shopping.core.tools.CommUtil;
import com.shopping.core.tools.Md5Encrypt;
import com.shopping.foundation.domain.*;
import com.shopping.foundation.domain.api.ApiUser;
import com.shopping.foundation.service.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by eollse on 2017/5/24.
 */
@Controller
public class ApiUserViewAction {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IIntegralLogService integralLogService;

    @Autowired
    private IAlbumService albumService;

    @Autowired
    private ISysLogService sysLogService;
    @Autowired
    private IGoodsCartService goodsCartService;
    @Autowired
    private IStoreCartService storeCartService;


    /**
     * 第三方登录
     *
     * @param request
     * @param mobileNum 手机号码
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/thirdPartyLogin.htm", produces = "application/json;charset=utf-8")
    @ResponseBody
    public String thirdPartyLogin(HttpServletRequest request, String mobileNum) throws IOException {
        boolean reg = true;
        String msg = "";
        Map params = new HashMap();
        params.put("mobile", mobileNum);
        List users = this.userService.query("select obj from User obj where obj.mobile=:mobile", params, -1, -1);
        if ((users != null) && (users.size() > 0)) {
            reg = false;
        }
        if (reg) {
            User user = new User();
            user.setUserName(mobileNum);
            user.setUserRole("BUYER");
            user.setAddTime(new Date());
            user.setMobile(mobileNum);
            user.setPassword(Md5Encrypt.md5("888888").toLowerCase());
            params.clear();
            params.put("type", "BUYER");
            List roles = this.roleService.query("select obj from Role obj where obj.type=:type", params, -1, -1);
            user.getRoles().addAll(roles);
            if (this.configService.getSysConfig().isIntegral()) {
                user.setIntegral(this.configService.getSysConfig().getMemberRegister());
                this.userService.save(user);
                IntegralLog log = new IntegralLog();
                log.setAddTime(new Date());
                log.setContent("用户注册增加" + this.configService.getSysConfig().getMemberRegister() + "分");
                log.setIntegral(this.configService.getSysConfig().getMemberRegister());
                log.setIntegral_user(user);
                log.setType("reg");
                this.integralLogService.save(log);
            } else {
                this.userService.save(user);
            }

            Album album = new Album();
            album.setAddTime(new Date());
            album.setAlbum_default(true);
            album.setAlbum_name("默认相册");
            album.setAlbum_sequence(-10000);
            album.setUser(user);
            this.albumService.save(album);
        }
        return this.login(request, mobileNum);
    }

    /**
     * 用户登录
     *
     * @param request
     * @param mobileNum 电话号码
     * @return
     */
    public String login(HttpServletRequest request, String mobileNum) {
        User user = this.userService.getObjByProperty("mobile", mobileNum);

        if (user != null) {
            ApiUser apiUser = new ApiUser();
            int totalSize = 0;
            apiUser.setId(user.getId());
            apiUser.setUserName(user.getUserName());
            apiUser.setUserRole(user.getUserRole());
            apiUser.setTelephone(user.getTelephone());
            apiUser.setIntegral(user.getIntegral());

            List<StoreCart> list = cart_calc(request, user);
            for (StoreCart sc : list) {
                int size = sc.getGcs().size();
                totalSize += size;
            }
            apiUser.setCartGoodsCount(totalSize);
            if (user.getPhoto() != null) {
                apiUser.setPath(user.getPhoto().getPath());
            }
            if (user.getArea() != null) {
                apiUser.setAreaName(user.getArea().getAreaName());
            }

            this.loginSuccess(request, user);
            return "{\"statusCode\":200,\"msg\":\"登录成功！\",\"data\":" + JSONObject.fromObject(apiUser) + "}";
        } else {
            return "{\"statusCode\":302,\"msg\":\"用户名错误！\"}";
        }

    }

    /**
     * 用户登录成功保存session信息
     *
     * @param request
     * @param user    用户对象
     */
    public void loginSuccess(HttpServletRequest request, User user) {
        user.setIntegral(user.getIntegral() + this.configService.getSysConfig().getMemberDayLogin());
        IntegralLog log = new IntegralLog();
        log.setAddTime(new Date());
        log.setContent("用户" + CommUtil.formatLongDate(new Date()) +
                "登录增加" + this.configService.getSysConfig().getMemberDayLogin() + "分");
        log.setIntegral(this.configService.getSysConfig().getMemberRegister());
        log.setIntegral_user(user);
        log.setType("login");
        this.integralLogService.save(log);

        user.setLoginDate(new Date());
        user.setLoginIp(CommUtil.getIpAddr(request));
        user.setLoginCount(user.getLoginCount() + 1);
        this.userService.update(user);
        HttpSession session = request.getSession(false);
        session.setAttribute("user", user);
        session.setAttribute("lastLoginDate", new Date());
        session.setAttribute("loginIp", CommUtil.getIpAddr(request));
        session.setAttribute("login", Boolean.valueOf(true));
    }

    /**
     * 登录注销
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/shopping_logout.htm")
    @ResponseBody
    public String accountLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User u = (User) session.getAttribute("user");
        if (u != null) {
            User user = this.userService.getObjById(u.getId());
            user.setLastLoginDate((Date) session.getAttribute("lastLoginDate"));
            user.setLastLoginIp((String) session.getAttribute("loginIp"));
            this.userService.update(user);
            SysLog log = new SysLog();
            log.setAddTime(new Date());
            log.setContent(user.getTrueName() + "于" +
                    CommUtil.formatTime("yyyy-MM-dd HH:mm:ss", new Date()) + "退出系统");
            log.setTitle("用户退出");
            log.setType(0);
            log.setUser(user);
            log.setIp(CommUtil.getIpAddr(request));
            this.sysLogService.save(log);
        }
        session.removeAttribute("user");
        session.removeAttribute("login");
        session.removeAttribute("role");
        session.removeAttribute("cart");
        ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest().getSession(false).removeAttribute("user");
        return "{\"statusCode\":200,\"msg\":\"退出成功！\"}";
    }


    private List<StoreCart> cart_calc(HttpServletRequest request, User user) {
        List<StoreCart> cart = new ArrayList<StoreCart>();
        List<StoreCart> user_cart = new ArrayList<StoreCart>();
        List<StoreCart> cookie_cart = new ArrayList<StoreCart>();
        if (user != null) {
            user = this.userService.getObjById(CommUtil.null2Long(user.getId()));
        }
        String cart_session_id = "";
        Map params = new HashMap();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("cart_session_id")) {
                    cart_session_id = CommUtil.null2String(cookie.getValue());
                }
            }
        }
        if (user != null) {
            if (!cart_session_id.equals("")) {
                if (user.getStore() != null) {
                    params.clear();
                    params.put("cart_session_id", cart_session_id);
                    params.put("user_id", user.getId());
                    params.put("sc_status", Integer.valueOf(0));
                    params.put("store_id", user.getStore().getId());
                    List<StoreCart> store_cookie_cart = this.storeCartService.query(
                            "select obj from StoreCart obj where (obj.cart_session_id=:cart_session_id or obj.user.id=:user_id) and obj.sc_status=:sc_status and obj.store.id=:store_id",
                            params, -1, -1);
                    for (StoreCart sc : store_cookie_cart) {
                        // sc = (StoreCart)localIterator1.next();
                        for (GoodsCart gc : ((StoreCart) sc).getGcs()) {
                            gc.getGsps().clear();
                            this.goodsCartService.delete(gc.getId());
                        }
                        this.storeCartService.delete(((StoreCart) sc).getId());
                    }
                }

                params.clear();
                params.put("cart_session_id", cart_session_id);
                params.put("sc_status", Integer.valueOf(0));
                cookie_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.cart_session_id=:cart_session_id and obj.sc_status=:sc_status",
                        params, -1, -1);

                params.clear();
                params.put("user_id", user.getId());
                params.put("sc_status", Integer.valueOf(0));
                user_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.user.id=:user_id and obj.sc_status=:sc_status", params,
                        -1, -1);
            } else {
                params.clear();
                params.put("user_id", user.getId());
                params.put("sc_status", Integer.valueOf(0));
                user_cart = this.storeCartService.query(
                        "select obj from StoreCart obj where obj.user.id=:user_id and obj.sc_status=:sc_status", params,
                        -1, -1);
            }

        } else if (!cart_session_id.equals("")) {
            params.clear();
            params.put("cart_session_id", cart_session_id);
            params.put("sc_status", Integer.valueOf(0));
            cookie_cart = this.storeCartService.query(
                    "select obj from StoreCart obj where obj.cart_session_id=:cart_session_id and obj.sc_status=:sc_status",
                    params, -1, -1);
        }

        for (StoreCart sc : user_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc.getStore().getId())) {
                    sc_add = false;
                }
            }
            if (sc_add) {
                cart.add(sc);
            }
        }
        for (StoreCart sc : cookie_cart) {
            boolean sc_add = true;
            for (StoreCart sc1 : cart) {
                if (sc1.getStore().getId().equals(sc.getStore().getId())) {
                    sc_add = false;
                    for (GoodsCart gc : sc.getGcs()) {
                        gc.setSc(sc1);
                        this.goodsCartService.update(gc);
                    }
                    this.storeCartService.delete(sc.getId());
                }
            }
            if (sc_add) {
                cart.add(sc);
            }
        }
        return (List<StoreCart>) cart;
    }
}

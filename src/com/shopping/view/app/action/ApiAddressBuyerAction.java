package com.shopping.view.app.action;

import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.query.support.IPageList;
import com.shopping.foundation.domain.User;
import com.shopping.foundation.domain.api.ApiAddress;
import com.shopping.foundation.domain.api.ApiArea;
import com.shopping.foundation.domain.query.AddressQueryObject;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Address;
import com.shopping.foundation.domain.Area;
import com.shopping.foundation.service.IAddressService;
import com.shopping.foundation.service.IAreaService;

@Controller
public class ApiAddressBuyerAction {


    @Autowired
    private IAddressService addressService;

    @Autowired
    private IAreaService areaService;


    /**
     * 用户收货地址列表
     *
     * @param request
     * @param currentPage 当前页
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/getAddressList.htm?currentPage=*
     */
    @RequestMapping({"/api/getAddressList.htm"})
    @ResponseBody
    public String getAddressList(HttpServletRequest request, String currentPage) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        List<ApiAddress> apiAddresses = new ArrayList<ApiAddress>();
        ApiAddress apiAddress;
        if (user != null) {
            Map<String, Object> map = new HashMap<String, Object>();
            AddressQueryObject qo = new AddressQueryObject(currentPage, map, null, null);
            qo.addQuery("obj.user.id", new SysMap("user_id", user.getId()), "=");
            qo.addQuery("obj.deleteStatus", new SysMap("deleteStatus", Boolean.valueOf(false)), "=");
            IPageList pList = this.addressService.list(qo);
            if (pList.getResult() != null) {
                List<Address> lists = pList.getResult();
                int size = lists.size();
                if (size > 0) {
                    for (int i = 0; i < size; i++) {
                        apiAddress = new ApiAddress();
                        apiAddress.setId(lists.get(i).getId());
                        apiAddress.setTrueName(lists.get(i).getTrueName());
                        apiAddress.setAreaId(lists.get(i).getArea().getId());
                        apiAddress.setAreaName(lists.get(i).getArea().getParent().getParent().getAreaName() +
                                lists.get(i).getArea().getParent().getAreaName() +
                                lists.get(i).getArea().getAreaName());
                        apiAddress.setArea_info(lists.get(i).getArea_info());
                        apiAddress.setMobile(lists.get(i).getMobile());
                        apiAddress.setTelephone(lists.get(i).getTelephone());
                        apiAddress.setZip(lists.get(i).getZip());
                        apiAddress.setDefaultStatus(lists.get(i).isDeleteStatus());
                        apiAddresses.add(apiAddress);
                    }
                }
            }
            String content = "{\"statusCode\":\"200\",\"msg\":\"数据加载成功！\",\"rowCount\":" + pList.getRowCount() + ",\"pages\":" + pList.getPages()
                    + ",\"pageCurrent\":" + pList.getCurrentPage() + ",\"list\":";
            String liststr = JSONArray.fromObject(apiAddresses).toString();
            content += liststr + "}";
            return content;
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }
    }

    /**
     * 获取地址区域信息
     *
     * @param parentId 上一级id,第一级id传 0
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/getAllAreaById.htm?parentId=*
     */
    @RequestMapping("/api/getAllAreaById.htm")
    @ResponseBody
    public String getAllAreaById(String parentId) {
        Map params = new HashMap();
        List<ApiArea> areas = new ArrayList<ApiArea>();

        if (parentId == null || parentId.equals("")) {
            parentId = "0";
        }

        String sql = "select obj from Area obj where obj.parent.id is null";
        if (!parentId.equals("0")) {
            sql = "select obj from Area obj where obj.parent.id=:parentId";
            params.put("parentId", Long.parseLong(parentId));
        }
        List<Area> lists = this.areaService.query(sql, params, -1, -1);
        int size = lists.size();
        if (size > 0) {
            ApiArea area;
            for (int i = 0; i < lists.size(); i++) {
                area = new ApiArea();
                area.setId(lists.get(i).getId());
                area.setAreaName(lists.get(i).getAreaName());
                area.setCommon(lists.get(i).isCommon());
                area.setLevel(lists.get(i).getLevel());
                area.setSequence(lists.get(i).getSequence());
                if (lists.get(i).getParent() != null) {
                    area.setPid(lists.get(i).getParent().getId());
                    area.setParentName(lists.get(i).getParent().getAreaName());
                } else {
                    area.setPid(0);
                    area.setParentName("中国");
                }
                areas.add(area);
            }
        }
        String content = "{\"statusCode\":\"200\",\"msg\":\"数据加载成功！\",\"rowCount\":" + size + ",\"data\":";
        content += JSONArray.fromObject(areas).toString() + "}";
        return content;
    }


    /**
     * 新增和修改地址保存方法
     *
     * @param request
     * @param id      修改收货地址时需传id
     * @param area_id 区域id
     * @param address trueName 真是姓名
     * @param address area_info  详细地址
     * @param address zip  邮编
     * @param address telephone 联系电话
     * @param address mobile 手机号码
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/address_save.htm?id=*&area_id=*&trueName=*&area_info=*&zip=*&telephone=*&mobile=*
     */
    @RequestMapping("/api/address_save.htm")
    @ResponseBody
    public String address_save(HttpServletRequest request, String id, String area_id, Address address) {
        HttpSession session = request.getSession(false);
        User user = (User) session.getAttribute("user");
        address.setAddTime(new Date());
        address.setUser(user);
        boolean result;
        if (user != null) {
            if (!area_id.equals("") && area_id != null) {
                Area area = this.areaService.getObjById(CommUtil.null2Long(area_id));
                address.setArea(area);
            } else {
                return "{\"statusCode\":\"201\",\"msg\":\"请选择区域信息!\"}";
            }

            if (id == null)
                id = "";
            if (id.equals("")) {
                result = this.addressService.save(address);
            } else {
                result = this.addressService.update(address);
            }

            if (result) {
                return "{\"statusCode\":\"200\",\"msg\":\"编辑地址保存成功!\"}";
            } else {
                return "{\"statusCode\":\"202\",\"msg\":\"编辑地址保存失败!\"}";
            }
        } else {
            return "{\"statusCode\":\"300\",\"msg\":\"请登录用户账号！\"}";
        }

    }


    /**
     * 删除用户收货地址方法
     *
     * @param id 收货地址id
     * @return
     * @apiNote http://192.168.1.223:8080/shopping/api/address_del.htm?id=*
     */
    @RequestMapping("/api/address_del.htm")
    @ResponseBody
    public String address_del(String id) {
        boolean result = false;
        if (!id.equals("") && id != null) {
            Address address = this.addressService.getObjById(Long.valueOf(Long.parseLong(id)));
            if (address != null) {
                address.setDeleteStatus(true);
                result = this.addressService.update(address);
            } else {
                return "{\"statusCode\":300,\"msg\":\"输入收货地址id有误!\"}";
            }
        }

        if (result) {
            return "{\"statusCode\":200,\"msg\":\"成功删除收货地址!\"}";
        } else {
            return "{\"statusCode\":201,\"msg\":\"删除收货地址失败!\"}";
        }
    }
}

package com.shopping.view.app.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.shopping.core.domain.virtual.SysMap;
import com.shopping.core.mv.JModelAndView;
import com.shopping.core.query.support.IPageList;
import com.shopping.core.tools.CommUtil;
import com.shopping.foundation.domain.Article;
import com.shopping.foundation.domain.ArticleClass;
import com.shopping.foundation.domain.api.ApiArticle;
import com.shopping.foundation.domain.query.ArticleQueryObject;
import com.shopping.foundation.service.IArticleClassService;
import com.shopping.foundation.service.IArticleService;
import com.shopping.foundation.service.ISysConfigService;
import com.shopping.foundation.service.IUserConfigService;
import com.shopping.view.web.tools.ArticleViewTools;

@Controller
public class ApiArticleViewAction {
	 @Autowired
	   private ISysConfigService configService;
	 
	   @Autowired
	   private IUserConfigService userConfigService;
	 
	   @Autowired
	   private IArticleService articleService;
	 
	   @Autowired
	   private IArticleClassService articleClassService;
	 
	   @Autowired
	   private ArticleViewTools articleTools;
	   
	   /**
	    * 商城新闻列表
	    * @param request
	    * @param response
	    * @param param
	    * @param currentPage
	    * @return
	    */
	   @RequestMapping({"/api/articlelist.htm"})
	   @ResponseBody
	   public String articlelist(HttpServletRequest request, HttpServletResponse response, String param, String currentPage)
	   {
//	     ModelAndView mv = new JModelAndView("articlelist.html", this.configService
//	       .getSysConfig(), this.userConfigService.getUserConfig(), 1, 
//	       request, response);
//	     ArticleClass ac = null;
//	     ArticleQueryObject aqo = new ArticleQueryObject();
//	     aqo.setCurrentPage(Integer.valueOf(CommUtil.null2Int(currentPage)));
//	     Long id = CommUtil.null2Long(param);
//	     String mark = "";
//	     if (id.longValue() == -1L) {
//	       mark = param;
//	     }
//	     if (!mark.equals("")) {
//	       aqo
//	         .addQuery("obj.articleClass.mark", 
//	         new SysMap("mark", mark), "=");
//	       ac = this.articleClassService.getObjByPropertyName("mark", mark);
//	     }
//	     if (id.longValue() != -1L) {
//	       aqo.addQuery("obj.articleClass.id", new SysMap("id", id), "=");
//	       ac = this.articleClassService.getObjById(id);
//	     }
//	     aqo.addQuery("obj.display", new SysMap("display", Boolean.valueOf(true)), "=");
//	     aqo.setOrderBy("addTime");
//	     aqo.setOrderType("desc");
//	     IPageList pList = this.articleService.list(aqo);
//	     String url = CommUtil.getURL(request) + "/articlelist_" + ac.getId();
//	     CommUtil.saveIPageList2ModelAndView("", url, "", pList, mv);
//	     List acs = this.articleClassService
//	       .query(
//	       "select obj from ArticleClass obj where obj.parent.id is null order by obj.sequence asc", 
//	       null, -1, -1);
	     List<Article> articles = this.articleService.query(
	       "select obj from Article obj order by obj.addTime desc", null, 
	       0, 6);
//	     Map<String,Object> map=new HashMap<String,Object>();
//	     map.put("articles", articles);
//	     mv.addObject("ac", ac);
//	     mv.addObject("articles", articles);
//	     mv.addObject("acs", acs);
//	     return mv;
//	     String data=JSONArray.fromObject(map.get("articles")).toString();
//	     List<Article>  article=new ArrayList<Article>();
//	     for(Article article1: articles){
//	    	 article=article1.getArticleClass().getArticles();
//	     }
	     List<Article>  article=articles.get(0).getArticleClass().getArticles();
	     List<ApiArticle> apiArticle=new ArrayList<ApiArticle>();
	     for(Article article1:article){
	    	 for(ApiArticle apiArticle1 : apiArticle){
	    		 apiArticle1.setTitle(article1.getTitle());
	    		 apiArticle1.setContent(article1.getContent());
	    		 System.out.println(apiArticle1);
	    		 apiArticle.add(apiArticle1);
	    	 }
	     }
//	     System.out.println(article);
	     
	     String data=JSON.toJSONString(apiArticle);
	     return "{\"statusCode\":200,\"msg\":"+data+"}";
	   }
	   
	   /**
	    * 商城新闻内容
	    * @param request
	    * @param response
	    * @param param
	    * @return
	    */
	   @RequestMapping({"/api/article.htm"})
	   @ResponseBody
	   public String article(HttpServletRequest request, HttpServletResponse response, String param) {
//	     ModelAndView mv = new JModelAndView("article.html", this.configService.getSysConfig(), 
//	    		 this.userConfigService.getUserConfig(), 1, request, response);
//	     Article obj = null;
//	     Long id = CommUtil.null2Long(param);
//	     String mark = "";
//	     if (id.longValue() == -1L) {
//	       mark = param;
//	     }
//	     if (id.longValue() != -1L) {
//	       obj = this.articleService.getObjById(id);
//	     }
//	     if (!mark.equals("")) {
//	       obj = this.articleService.getObjByProperty("mark", mark);
//	     }
//	     List acs = this.articleClassService.query(
//	       "select obj from ArticleClass obj where obj.parent.id is null order by obj.sequence asc", null, -1, -1);
	     List articles = this.articleService.query(
	       "select obj from Article obj order by obj.addTime desc", null, 0, 6);
//	     Map<String,Object> map=new HashMap<String,Object>();
//	     map.put("articles", articles);
////	     mv.addObject("ac", ac);
////	     mv.addObject("articles", articles);
////	     mv.addObject("acs", acs);
////	     return mv;
//	     String data=JSONArray.fromObject(map.get("articles")).toString();
	     String data=JSON.toJSONString(articles);
	     return "{\"statusCode\":200,\"msg\":"+data+"}";
	   
	   }
}

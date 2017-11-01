 package com.shopping.foundation.domain.query;
 
 import org.springframework.web.servlet.ModelAndView;

import com.shopping.core.query.QueryObject;

 import java.util.Map;

 public class AddressQueryObject extends QueryObject
 {
   public AddressQueryObject(String currentPage, ModelAndView mv, String orderBy, String orderType)
   {
     super(currentPage, mv, orderBy, orderType);
   }

     public AddressQueryObject(String currentPage, Map<String, Object> map, String orderBy, String orderType)
     {
         super(currentPage, map, orderBy, orderType);
     }
 
   public AddressQueryObject()
   {
   }
 }



 
 
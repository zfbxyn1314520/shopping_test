package com.shopping.foundation.domain.api;

import javax.persistence.Column;
import javax.persistence.Lob;

public class ApiArticle {
	//文章标题
		private String title;
		//文章类
//		@ManyToOne(fetch = FetchType.LAZY)
//		private ArticleClass articleClass;
//		//文章地址
//		private String url;
//		//文章序列
//		private int sequence;
		//文章是否显示
//		private boolean display;
//		//文字标记
//		private String mark;
		//文章内容
		@Lob
		@Column(columnDefinition = "LongText")
		private String content;
		
		public ApiArticle() {
			super();
			// TODO Auto-generated constructor stub
		}

		public ApiArticle(String title, String content) {
			super();
			this.title = title;
			this.content = content;
		}

		@Override
		public String toString() {
			return "ApiArticle [title=" + title + ", content=" + content + "]";
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
		
		
}

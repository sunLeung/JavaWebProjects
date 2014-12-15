package common.router;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.ArrayUtils;

import common.config.Config;
import common.user.AuthMap;
import common.user.User;
import common.user.UserService;

@WebFilter(filterName="rootFilter",urlPatterns="/*")
public class RootFilter implements Filter{
	/**放行资源*/
	private static List<String> freeRequest=Arrays.asList("/user/login.do","login.jsp","test.jsp",".js",".css",".jpeg",".jpg",".gif",".png",".ico",".svg",".ttf",".woff");

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		//setting charset to utf-8
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		HttpSession session = req.getSession();
		User user=null;
		req.setCharacterEncoding("utf-8");
		resp.setCharacterEncoding("utf-8");
		resp.setHeader("Pragma", "No-cache");
		resp.setHeader("Cache-Control","no-cache");
		resp.setDateHeader("Expires", 0);
		
		String uri=req.getRequestURI();
		System.out.println(uri);
		
		if(uri.contains("/logs/")){
			resp.setStatus(403);
			resp.getWriter().write("Forbidden");
			return;
		}
		
		if(isFreeRequest(uri)){
			chain.doFilter(req, resp);
			return;
		}else{
			user=(User)session.getAttribute("user");
		}
		
		if(user==null){
			resp.sendRedirect(Config.WEB_BASE+"/login.jsp");
			return;
		}else{
			if(hasAuth(uri, user)){
				chain.doFilter(req, resp);
				return;
			}
		}
		
		resp.setStatus(403);
		resp.getWriter().write("Forbidden");
	}

	/**
	 * 验证改URI是否公开访问
	 * @param uri
	 * @return
	 */
	public static boolean isFreeRequest(String uri){
		for(String end:freeRequest){
			if(uri.endsWith(end)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 验证用户是否有权限访问该URI
	 * @param uri
	 * @param user
	 * @return
	 */
	public static boolean hasAuth(String uri,User user){
		if(user!=null){
			AuthMap authMap=UserService.authContent.get(uri);
			if(authMap==null)return true;
			if(authMap!=null&&ArrayUtils.contains(user.authArray(), authMap.getAuthCode())){
				return true;
			}
		}
		return false;
	}
}

package cn.v5.interceptor;

import cn.v5.entity.CurrentUser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: qgan(qgan@v5.cn)
 * Date: 14-3-10
 * Time: 下午2:34
 * To change this template use File | Settings | File Templates.
 */
public class PreCleanInterceptor extends ConfigurableInterceptor {
    /**
     * 请求入口时，清理资源
     *
     * @param request
     * @param response
     * @param handler
     * @throws Exception
     */
    @Override
    public boolean internalPreHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CurrentUser.clearUser();
        return true;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return super.preHandle(request, response, handler);
    }

    @Override
    public void internalAfterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        super.internalAfterCompletion(request, response, handler, ex);
    }
}

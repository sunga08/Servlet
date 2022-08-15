package hello.servlet.web.frontcontroller.v5;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV3HandlerAdapter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {

    //private Map<String, ControllerV4> controllerMap = new HashMap<>();
    private final Map<String, Object> handlerMappingMap = new HashMap<>(); //핸들러 매핑 정보
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>(); //핸들러 어댑터 목록

    public FrontControllerServletV5(){
        initHandlerMappingMap();
        initHandlerAdapters();
    }

    private void initHandlerMappingMap() {
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV3HandlerAdapter());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //1. 핸들러 조회
        Object handler = getHandler(request);

        if(handler==null){
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //2. 핸들러 어댑터 조회
        MyHandlerAdapter adapter = getHandlerAdapter(handler);

        //3,4. 핸들러 어댑터 호출 -> 핸들러(컨트롤러) 호출 -> ModelView 반환
        ModelView mv = adapter.handle(request, response, handler);

        String viewName = mv.getViewName();

        //5. ViewResolver 호출
        MyView view = viewResolver(viewName);

        view.render(mv.getModel(), request, response);
    }

    private MyHandlerAdapter getHandlerAdapter(Object handler) {

        for (MyHandlerAdapter adapter : handlerAdapters) {
            if(adapter.supports(handler)){
                return adapter;
            }
        }

        throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler= "+handler);
    }

    //요청정보를 가지고 핸들러맵에서 핸들러를 찾는 로직
    private Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        //ControllerV3 controller = controllerMap.get(requestURI);
        return handlerMappingMap.get(requestURI);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }

}

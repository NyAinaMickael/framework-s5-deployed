package mg.itu.ermite.framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.ermite.framework.annotation.JsonResponse;
import mg.itu.ermite.framework.util.EndPointDetails;
import mg.itu.ermite.framework.util.ModelView;
import mg.itu.ermite.framework.util.Reflection;

//Ataoko configurable par fichier de configuration ty aveo
// @MultipartConfig(
//     maxFileSize = 1024 * 1024 * 10,      // 10MB
//     maxRequestSize = 1024 * 1024 * 50    // 50MB
// )
public class FrontServlet extends HttpServlet{
    
    private RequestDispatcher defaultDispatcher ;

    @Override
    public void init() throws ServletException {
        defaultDispatcher = getServletContext().getNamedDispatcher("default");
        if(defaultDispatcher == null) throw new ServletException("Servlet par defaut introuvable");
    }

    private EndPointDetails findEndPoint(List<EndPointDetails> endPointDetails,String httpMethod,String url) throws Exception
    {
        EndPointDetails matchedEndPoint = null;
        for (EndPointDetails endPoint : endPointDetails) {
            if(endPoint.getHttpMethod().equals(httpMethod) )
            {
                matchedEndPoint = endPoint;
                break;
            }
        }
        if(matchedEndPoint == null)
        {
            for (EndPointDetails endPoint : endPointDetails) {
                if(endPoint.getHttpMethod().equals("*") )
                {
                    matchedEndPoint = endPoint;
                    break;
                }
            }
        }

        if(matchedEndPoint == null)
        {
            throw new Exception("Aucun endpoint n'est defini pour la requete '"+httpMethod+" "+url+"'");
        }


        return matchedEndPoint;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        String path = pathInfo != null ? pathInfo : request.getServletPath();
        if(path != null && getServletContext().getResource(path) != null) {
            defaultDispatcher.forward(request, response);
            return; 
        }   

        String httpMethod = request.getMethod();

        String url = request.getRequestURI().substring(request.getContextPath().length());
        
        try {
            Map<String,List<EndPointDetails>> endpoints= (Map<String,List<EndPointDetails>>) request.getServletContext().getAttribute("endpoints");

            Map<String, String> urlParams = new HashMap<>();

            List<EndPointDetails> endPointList = endpoints.get(url);

            if(endPointList == null)
            {
                // Andramana jerena ihany hoe sao misy possibilite ahitana uri misy /{valeur} mety hifanaraka amle url tape
                for (Map.Entry<String, List<EndPointDetails>> e : endpoints.entrySet()) {
                    String key = e.getKey();

                    // Construire un regex à partir de la clé en remplaçant {param} par ([^/]+)
                    String patternStr = key.replaceAll("\\{[^/]+\\}", "([^/]+)");
                    patternStr = "^" + patternStr + "$";

                    if (url.matches(patternStr)) {
                        endPointList = e.getValue();

                        List<String> paramNames = new ArrayList<>();
                        Pattern paramNamePattern = java.util.regex.Pattern.compile("\\{([^/]+)\\}");
                        Matcher paramNameMatcher = paramNamePattern.matcher(key);
                        
                        while (paramNameMatcher.find()) {
                            paramNames.add(paramNameMatcher.group(1));
                        }
                        Pattern valuePattern = java.util.regex.Pattern.compile(patternStr);
                        Matcher valueMatcher = valuePattern.matcher(url);
                        
                        if (valueMatcher.matches()) {
                            for (int i = 0; i < paramNames.size(); i++) {
                                // group(0) c'est toute la chaîne, donc on commence à 1
                                String paramValue = valueMatcher.group(i + 1);
                                urlParams.put(paramNames.get(i), paramValue);
                            }
                        }
                        break;
                    }
                }
                // Sinon raha tena tsy misy fika dia on leve une exception
                if(endPointList == null)
                {
                    throw new Exception("Aucun endpoint enregistre pour l'URL :"+url);
                }
            }

            EndPointDetails endPoint = findEndPoint((List<EndPointDetails>) endPointList, httpMethod, url);
            
            if (endPoint.getMethod().isAnnotationPresent(JsonResponse.class)) {
                response.setContentType("application/json;charset=UTF-8");
                
                PrintWriter out = response.getWriter();
                ObjectMapper mapper = new ObjectMapper();
                
                try {
                    Object endpointResponse = endPoint.invokeMethod(request, urlParams);
                    
                    Map<String, Object> jsonResponse = new LinkedHashMap<>();
                    jsonResponse.put("status", "success");
                    jsonResponse.put("code", 200);
                    jsonResponse.put("data", endpointResponse);
                    
                    if (endpointResponse != null) {
                        if (Reflection.isCollectionType(endpointResponse.getClass())) {
                            jsonResponse.put("count", ((Collection<?>) endpointResponse).size());
                        } else if (endpointResponse.getClass().isArray()) {
                            jsonResponse.put("count", Array.getLength(endpointResponse));
                        }
                    }
                    
                    mapper.writeValue(out, jsonResponse);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    
                    Map<String, Object> errorResponse = new LinkedHashMap<>();
                    errorResponse.put("status", "error");
                    errorResponse.put("code", 500);
                    errorResponse.put("message", e.getMessage());
                    errorResponse.put("data", null);
                    
                    try {
                        mapper.writeValue(out, errorResponse);
                    } catch (JsonProcessingException jpe) {
                        // Fallback en cas d'erreur de sérialisation
                        out.print("{\"status\":\"error\",\"code\":500,\"message\":\"Internal server error\"}");
                    }
                }
                
                out.close();
            }
            else{
                Object endpointResponse = endPoint.invokeMethod(request,urlParams);

                if(endpointResponse.getClass().equals(String.class))
                {
                    PrintWriter out = response.getWriter();
                    out.println(endpointResponse);
                    out.close();
                }    
                else if(endpointResponse.getClass().equals(ModelView.class))
                {
                    ModelView modelView = (ModelView) endpointResponse;
    
                    //ajout des attributs
                    for (Map.Entry<String,Object> attribute : modelView.getAttributes().entrySet()) {
                        request.setAttribute(attribute.getKey(), attribute.getValue());
                    }
    
                    request.getRequestDispatcher(modelView.getView()).forward(request, response);
                }
            }
            

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la resolution de l'URL:"+e.getMessage());
            response.setContentType("text/html");
            
            PrintWriter out = response.getWriter();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Fallback</title></head>");
            out.println("<body>");
            out.println("<p>Vous avez tapé : <strong>" + url + "</strong> </p>");
            out.println("</body>");
            out.println("</html>");
        }
        
        

    }
}

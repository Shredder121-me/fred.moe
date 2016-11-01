package com.frederikam.fred.moe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Configuration
@EnableAutoConfiguration(exclude = {HibernateJpaAutoConfiguration.class, DataSourceAutoConfiguration.class})
@Controller
@ComponentScan
public class FredDotMoe {

    public static final long MAX_UPLOAD_SIZE = 128 * 1000000;
    private static final Pattern FILE_EXTENSION_PATTERN = Pattern.compile("(\\.\\w+)$");

    public static void main(String[] args) throws FileNotFoundException {
        InputStream is = new FileInputStream(new File("./config.json"));
        Scanner scanner = new Scanner(is);
        JSONObject config = new JSONObject(scanner.useDelimiter("\\A").next());
        ResourceManager.dataDir = new File(config.getString("dataDir"));
        
        scanner.close();
        ResourceManager.dataDir.mkdirs();
        ApplicationContext ctx = SpringApplication.run(FredDotMoe.class, args);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    private static String get(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(404);
        return "";
    }

    @PostMapping("/upload")
    @ResponseBody
    private static String upload(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam("file") final MultipartFile file
    ) throws IOException {
        //Check if the file limit is reached
        if (file.getSize() > MAX_UPLOAD_SIZE) {
            response.sendError(413);
        }
        
        //No .exe files please
        if(file.getOriginalFilename().toLowerCase().endsWith(".exe")){
            response.sendError(400);
        }
        
        String extension = "";
        Matcher m = FILE_EXTENSION_PATTERN.matcher(request.getParameter("name"));
        if(m.find()){
            extension = m.group(1);
        }
        
        File f = ResourceManager.getResource(ResourceManager.getUniqueName(extension));
        
        file.transferTo(f);

        return "";
    }

}

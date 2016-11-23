package cn.v5.web.controller;


import cn.v5.entity.User;
import cn.v5.service.UserService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Hashtable;

/**
 * Created by lb on 14/11/13.
 */
@Controller
@RequestMapping(value = "/qr")
public class QrcodeController {
    @Inject
    private UserService userService;

    @Value("${base.host}")
    private String baseHost;

    @RequestMapping(value = "/barcode/{id}", method = RequestMethod.GET)
    public void qrcode(@PathVariable String id, HttpServletResponse response) throws IOException {
        String url = baseHost + "/qr/" + id;
        response.setContentType("image/jpeg");

        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        hints.put(EncodeHintType.MARGIN, 0);


        try {

            BitMatrix matrix = new MultiFormatWriter().encode(url,
                    BarcodeFormat.QR_CODE, 160, 160, hints);

            MatrixToImageWriter.writeToStream(matrix, "jpeg", response.getOutputStream());

            response.getOutputStream().flush();
        } catch (Exception e) {

            e.printStackTrace();

        }


    }

    @RequestMapping(value = "/user/{account}", method = RequestMethod.GET)
    public String user(@PathVariable String account) {
        return "redirect:cgtp://name_card?account=" + account;
    }


    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public String card(@PathVariable String userId, Model model) {
        User user = userService.findById(userId);
        if (user == null) {
            return "notfound";
        }
        if (user.getAccount() == null) user.setAccount("unknown");
        model.addAttribute("user", user);

        return "card";
    }


}

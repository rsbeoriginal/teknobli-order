package com.tecknobli.order.service.Impl;

import com.tecknobli.order.dto.*;
import com.tecknobli.order.entity.PurchasedItem;
import com.tecknobli.order.entity.UserOrder;
import com.tecknobli.order.merchantmicroservice.dto.MerchantDTO;
import com.tecknobli.order.merchantmicroservice.dto.MerchantOrderDTO;
import com.tecknobli.order.productmicroservice.Endpoints;
import com.tecknobli.order.repository.PurchasedItemRepository;
import com.tecknobli.order.repository.UserOrderRepository;
import com.tecknobli.order.reviewmicroservices.EndPoints;
import com.tecknobli.order.reviewmicroservices.dto.ProductRatingDTO;
import com.tecknobli.order.service.CartService;
import com.tecknobli.order.service.EmailService;
import com.tecknobli.order.service.UserOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
public class UserOrderServiceImpl implements UserOrderService {

    @Autowired
    UserOrderRepository userOrderRepository;

    @Autowired
    PurchasedItemRepository purchasedItemRepository;

    @Autowired
    CartService cartService;

    @Autowired
    EmailService emailService;

    @Override
    @Transactional(readOnly = false)
    public UserOrder save(UserOrder userOrder) {

        UserOrder userOrderCreated = null;

        String userId = userOrder.getUserId();
        List<ProductDTO> productDTOList = cartService.findByUserId(userId).getProducts();

        Boolean orderValid = false;

        orderValid = validateOrder(userOrder, productDTOList);
        if (orderValid) {
            System.out.println("orderValid");
            userOrder.setOrderTimeStamp(new Date());
            userOrderCreated = userOrderRepository.save(userOrder);
            List<PurchasedItem> purchasedItemList = new ArrayList<>();
            for (ProductDTO productDTO : productDTOList) {
                PurchasedItem purchasedItem = new PurchasedItem();
                purchasedItem.setMerchantId(productDTO.getMerchantId());
                purchasedItem.setPrice(productDTO.getPrice());
                purchasedItem.setProductId(productDTO.getProductId());
                purchasedItem.setQuantity(productDTO.getQuantity());
                purchasedItem.setUserOrderId(userOrder);
                purchasedItemList.add(purchasedItem);
                purchasedItemRepository.save(purchasedItem);
            }
            userOrderCreated.setPurchasedItemList(purchasedItemList);
            cartService.deleteByUserId(userId);
            //Send Mail to user
            sendMailToUser(userOrderCreated);
            //now save order in merchant microservice
            sendOrderToMerchant(userOrderCreated, productDTOList);
        }


        return userOrderCreated;
    }

    private Boolean validateOrder(UserOrder userOrder, List<ProductDTO> productDTOList) {
        RecieptDTO recieptDTO = new RecieptDTO();
        recieptDTO.setUserOrderData(userOrder);
        List<RecieptProductDTO> recieptProductDTOList = new ArrayList<>();
        System.out.println("size: " + productDTOList.size());
        for (ProductDTO purchasedItem : productDTOList) {
            System.out.println("product" + purchasedItem.getProductName());
            RecieptProductDTO recieptProductDTO = new RecieptProductDTO();
            ProductDTO productDTO = getProduct(purchasedItem.getProductId());
            MerchantDTO merchantDTO = getMerchant(purchasedItem.getMerchantId());
            recieptProductDTO.setProductData(productDTO);
            recieptProductDTO.setMerchantData(merchantDTO);
            recieptProductDTO.setPrice(Double.valueOf(purchasedItem.getPrice()));
            recieptProductDTO.setQuantity(purchasedItem.getQuantity());
            recieptProductDTOList.add(recieptProductDTO);
        }
        recieptDTO.setRecieptProductDTOList(recieptProductDTOList);
        RestTemplate restTemplate = new RestTemplate();
        String URL = com.tecknobli.order.merchantmicroservice.Endpoints.BASE_URL + com.tecknobli.order.merchantmicroservice.Endpoints.VALIDATE_URL;
        System.out.println("url: " + URL);
        Boolean result = restTemplate.postForObject(URL, recieptDTO, Boolean.class);
        return result;
    }

    private void sendOrderToMerchant(UserOrder userOrderCreated, List<ProductDTO> productDTOList) {

//        List<MerchantOrderDTO> merchantOrderDTOList = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();
        for (ProductDTO productDTO : productDTOList) {
            MerchantOrderDTO merchantOrderDTO = new MerchantOrderDTO();
            merchantOrderDTO.setMerchantId(productDTO.getMerchantId());
            merchantOrderDTO.setProductId(productDTO.getProductId());
            merchantOrderDTO.setOrderId(userOrderCreated.getUserOrderId());

            String URL = com.tecknobli.order.merchantmicroservice.Endpoints.BASE_URL + com.tecknobli.order.merchantmicroservice.Endpoints.ADDORDER_URL;
            restTemplate.postForEntity(URL, merchantOrderDTO, MerchantOrderDTO.class);
        }


    }

    private void sendMailToUser(UserOrder userOrderCreated) {

        String subject = "Your Order: " + userOrderCreated.getUserOrderId();
        Double totalPrice = 0d;

        String htmlTemplate = initializeHTMLTemplate(userOrderCreated);
        for (PurchasedItem purchasedItem : userOrderCreated.getPurchasedItemList()) {

//            ProductDTO productDTO = getProduct(purchasedItem.getProductId());
//            body += "\n<strong>Product Name:</strong> " + productDTO.getProductName();
//            body += "\nPrice: " + purchasedItem.getPrice();
//            body += "\nQuantity: " + purchasedItem.getQuantity();

            totalPrice += (purchasedItem.getPrice() * purchasedItem.getQuantity());


            htmlTemplate += addProductToHTMl(purchasedItem);

        }

//        body += "\n\nTotal : " + totalPrice + "</body></html>";
        htmlTemplate += totalPriceHTML(totalPrice);

        emailService.sendSimpleMessage(userOrderCreated.getEmailId(),
                subject,
                htmlTemplate);


    }

    private String totalPriceHTML(Double totalPrice) {
        String html = "<tr>\n" +
                "                            <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "\n" +
                "                          <tr>\n" +
                "                            <td class=\"Spacer\" bgcolor=\"e6ebf1\" colspan=\"3\" height=\"1\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "\n" +
                "                          <tr>\n" +
                "                            <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"8\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "                          <tr>\n" +
                "                            <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "                          <tr>\n" +
                "                            <td class=\"Table-description Font Font--body Font--alt\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;font-weight: bold;\">\n" +
                "                                  Amount paid\n" +
                "                            </td>\n" +
                "                            <td class=\"Spacer Table-gap\" width=\"8\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                            <td class=\"Table-amount Font Font--body Font--alt\" align=\"right\" valign=\"top\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;font-weight: bold;\">\n" +
                "                                  ₹ "+totalPrice+"\n" +
                "                            </td>\n" +
                "                          </tr>\n" +
                "                          <tr>\n" +
                "                            <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "                          </tbody>\n" +
                "                        </table>\n" +
                "                      </td>\n" +
                "                      <td class=\"Spacer Spacer--gutter\" width=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                    </tr>\n" +
                "                    <tr><td class=\"Spacer Spacer--divider\" colspan=\"3\" height=\"4\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr>\n" +
                "                    </tbody>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "                <td class=\"Spacer Spacer--kill\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "              </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "\n" +
                "            <table class=\"Section Divider Divider--large\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"44\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "              <table class=\"Section Separator\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "                <tbody>\n" +
                "                <tr>\n" +
                "                  <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                  <td class=\"Spacer\" bgcolor=\"e6ebf1\" height=\"1\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                  <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                </tr>\n" +
                "                </tbody>\n" +
                "              </table>\n" +
                "\n" +
                "              <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"32\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "\n" +
                "                <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"16\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "              <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"16\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "          \n" +
                "\n" +
                "            <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"32\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "            <table class=\"Section Divider Divider--small\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "            <table class=\"Section Copy\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "              <tbody>\n" +
                "              <tr>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                <td class=\"Content Footer-legal Font Font--caption Font--mute\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #8898aa;font-size: 12px;line-height: 16px;\">\n" +
                "                  You're receiving this email because you made a purchase at Teknobli.\n" +
                "                </td>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "              </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "            </div>\n" +
                "            </td></tr></tbody></table></div>\n" +
                "        \n" +
                "        \n" +
                "      \n" +
                "      <table class=\"Divider Divider--small Divider--kill\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "    \n" +
                "    \n" +
                "    \n" +
                "</body>\n" +
                "</html>";
        return html;
    }

    private String initializeHTMLTemplate(UserOrder userOrderCreated) {

        SimpleDateFormat format = new SimpleDateFormat("\"EEE, d MMM yyyy");
        String date = format.format(userOrderCreated.getOrderTimeStamp());

        String html="<html>\n" +
                "<head>\n" +
                "      <title>Your receipt from Teknobli</title>\n" +
                "      <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "      <meta name=\"viewport\" content=\"width=device-width\">\n" +
                "      <meta name=\"robots\" content=\"noindex, nofollow\">\n" +
                "      <meta name=\"googlebot\" content=\"noindex, nofollow, noarchive\">\n" +
                "      <style type=\"text/css\">\n" +
                "        img + div {\n" +
                "          display: none !important; /* Hides image downloading in Gmail */\n" +
                "        }\n" +
                "        @media screen and (max-width: 600px) {\n" +
                "          /** Gmail **/\n" +
                "          *[class=\"Gmail\"] {\n" +
                "            display: none !important\n" +
                "          }\n" +
                "          /** Wrapper **/\n" +
                "          .Wrapper {\n" +
                "            max-width: 600px !important;\n" +
                "            min-width: 320px !important;\n" +
                "            width: 100% !important;\n" +
                "            border-radius: 0 !important;\n" +
                "          }\n" +
                "          .Section {\n" +
                "            width: 100% !important;\n" +
                "          }\n" +
                "          .Section--last {\n" +
                "            border-bottom-left-radius: 0 !important;\n" +
                "            border-bottom-right-radius: 0 !important;\n" +
                "          }\n" +
                "          /** Notice **/\n" +
                "          .Notice {\n" +
                "            border-bottom-left-radius: 0 !important;\n" +
                "            border-bottom-right-radius: 0 !important;\n" +
                "          }\n" +
                "          /** Header **/\n" +
                "          .Header .Header-left,\n" +
                "          .Header .Header-right {\n" +
                "            border-top-left-radius: 0 !important;\n" +
                "            border-top-right-radius: 0 !important;\n" +
                "          }\n" +
                "          /** Content **/\n" +
                "          .Content {\n" +
                "            width: auto !important;\n" +
                "          }\n" +
                "          /** Divider **/\n" +
                "          .Divider--kill {\n" +
                "            display: none !important;\n" +
                "            height: 0 !important;\n" +
                "            width: 0 !important;\n" +
                "          }\n" +
                "          /** Spacer **/\n" +
                "          .Spacer--gutter {\n" +
                "            width: 20px !important;\n" +
                "          }\n" +
                "          .Spacer--kill {\n" +
                "            height: 0 !important;\n" +
                "            width: 0 !important;\n" +
                "          }\n" +
                "          .Spacer--emailEnds {\n" +
                "            height: 0 !important;\n" +
                "          }\n" +
                "          /** Target **/\n" +
                "          .Target img {\n" +
                "            display: none !important;\n" +
                "            height: 0 !important;\n" +
                "            margin: 0 !important;\n" +
                "            max-height: 0 !important;\n" +
                "            min-height: 0 !important;\n" +
                "            mso-hide: all !important;\n" +
                "            padding: 0 !important;\n" +
                "            width: 0 !important;\n" +
                "            font-size: 0 !important;\n" +
                "            line-height: 0 !important;\n" +
                "          }\n" +
                "          .Target::before {\n" +
                "            content: '' !important;\n" +
                "            display: block !important;\n" +
                "          }\n" +
                "          /** Header **/\n" +
                "          .Header-area {\n" +
                "            width: 100% !important;\n" +
                "          }\n" +
                "          .Header-left,\n" +
                "          .Header-left::before,\n" +
                "          .Header-right,\n" +
                "          .Header-right::before {\n" +
                "            height: 156px !important;\n" +
                "            width: auto !important;\n" +
                "            background-size: 252px 156px !important;\n" +
                "          }\n" +
                "          .Header-left {\n" +
                "            background-image: url('https://stripe-images.s3.amazonaws.com/notifications/hosted/20180110/Header/Left.png') !important;\n" +
                "            background-position: bottom right !important;\n" +
                "          }\n" +
                "          .Header-right {\n" +
                "            background-image: url('https://stripe-images.s3.amazonaws.com/notifications/hosted/20180110/Header/Right.png') !important;\n" +
                "            background-position: bottom left !important;\n" +
                "          }\n" +
                "          .Header-icon,\n" +
                "          .Header-icon::before {\n" +
                "            width: 96px !important;\n" +
                "            height: 156px !important;\n" +
                "            background-size: 96px 156px !important;\n" +
                "          }\n" +
                "          .Header-icon {\n" +
                "            width: 96px !important;\n" +
                "            height: 156px !important;\n" +
                "            background-image: url('https://stripe-images.s3.amazonaws.com/emails/acct_20fyhGDC0ObZ9nUhH3S1/13/twelve_degree_icon@2x.png') !important;\n" +
                "            background-position: bottom center !important;\n" +
                "          }\n" +
                "          /** Table **/\n" +
                "          .Table-content {\n" +
                "            width: auto !important;\n" +
                "          }\n" +
                "          .Table-rows {\n" +
                "            width: 100% !important;\n" +
                "          }\n" +
                "        }\n" +
                "        @media screen and (max-width: 599px) {\n" +
                "          /** Data Blocks **/\n" +
                "          .DataBlocks-item {\n" +
                "            display: block !important;\n" +
                "            width: 100% !important;\n" +
                "          }\n" +
                "          .DataBlocks-spacer {\n" +
                "            display: block !important;\n" +
                "            height: 12px !important;\n" +
                "            width: auto !important;\n" +
                "          }\n" +
                "        }\n" +
                "      </style>\n" +
                "    </head>\n" +
                "    <body class=\"Email\" style=\"margin: 0;padding: 0;border: 0;background-color: #f1f5f9;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;-webkit-text-size-adjust: 100%;-ms-text-size-adjust: 100%;min-width: 100% !important;width: 100% !important;\">\n" +
                "    <div class=\"Preheader\" style=\"display: none !important;max-height: 0;max-width: 0;mso-hide: all;overflow: hidden;color: #ffffff;font-size: 1px;line-height: 1px;opacity: 0;visibility: hidden;\"></div>\n" +
                "    <div class=\"Background\" style=\"min-width: 100%;width: 100%;background-color: #f1f5f9;\">\n" +
                "      <table class=\"Wrapper\" align=\"center\" style=\"border: 0;border-collapse: collapse;margin: 0 auto !important;padding: 0;max-width: 600px;min-width: 600px;width: 600px;\">\n" +
                "        <tbody>\n" +
                "        <tr>\n" +
                "          <td style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;\">\n" +
                "\n" +
                "\n" +
                "            <table class=\"Divider Divider--small Divider--kill\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "                <div class=\"Shadow\" style=\"border-bottom-left-radius: 5px;border-bottom-right-radius: 5px;box-shadow: 0 7px 14px 0 rgba(50,50,93,0.10), 0 3px 6px 0 rgba(0,0,0,0.07);\">\n" +
                "                  <table class=\"Section Header\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "            <tbody>\n" +
                "            <tr>\n" +
                "              <td class=\"Header-left Target\" style=\"background-color: #4f3a57;border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-size: 0;line-height: 0px;mso-line-height-rule: exactly;background-size: 100% 100%;border-top-left-radius: 5px;\" align=\"right\" height=\"156\" valign=\"bottom\" width=\"252\">\n" +
                "                <a href=\"#\" style=\"pointer-events: none;\" target=\"_blank\">\n" +
                "                  <img alt=\"\" height=\"156\" width=\"252\" src=\"https://stripe-images.s3.amazonaws.com/notifications/hosted/20180110/Header/Left.png\" style=\"display: block;border: 0;line-height: 100%;width: 100%;\">\n" +
                "                </a>\n" +
                "              </td>\n" +
                "              <td class=\"Header-icon Target\" style=\"background-color: #4f3a57;border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-size: 0;line-height: 0px;mso-line-height-rule: exactly;background-size: 100% 100%;\" align=\"center\" height=\"156\" valign=\"bottom\" width=\"96\">\n" +
                "                  <img alt=\"\" height=\"156\" width=\"96\" src=\"https://firebasestorage.googleapis.com/v0/b/test-1d656.appspot.com/o/0cf99030-dfe5-4fb3-9f16-8f49f40ab591.png?alt=media&token=ed63d386-95b5-4933-8e04-ff8fc14e7828\" style=\"display: block;border: 0;line-height: 100%;width: 100%;\">\n" +
                "              </td>\n" +
                "              <td class=\"Header-right Target\" style=\"background-color: #4f3a57;border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-size: 0;line-height: 0px;mso-line-height-rule: exactly;background-size: 100% 100%;border-top-right-radius: 5px;\" align=\"left\" height=\"156\" valign=\"bottom\" width=\"252\">\n" +
                "                <a href=\"#\" style=\"pointer-events: none;\" target=\"_blank\">\n" +
                "                  <img alt=\"\" height=\"156\" width=\"252\" src=\"https://stripe-images.s3.amazonaws.com/notifications/hosted/20180110/Header/Right.png\" style=\"display: block;border: 0;line-height: 100%;width: 100%;\">\n" +
                "                </a>\n" +
                "              </td>\n" +
                "            </tr>\n" +
                "            </tbody>\n" +
                "            </table>\n" +
                "\n" +
                "            <table class=\"Section Title\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "              <tbody>\n" +
                "              <tr>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                <td class=\"Content Title-copy Font Font--title\" align=\"center\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #32325d;font-size: 24px;line-height: 32px;\">\n" +
                "                    Receipt from Teknobli\n" +
                "                </td>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "              </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "\n" +
                "            <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"8\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "                <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"4\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "                <table class=\"Section Title\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "                  <tbody>\n" +
                "                  <tr>\n" +
                "                    <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                    <td class=\"Content Title-copy Font Font--title\" align=\"center\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #8898aa;font-size: 15px;line-height: 18px;\">\n" +
                "                      Receipt "+userOrderCreated.getUserOrderId()+"\n" +
                "                    </td>\n" +
                "                    <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                  </tr>\n" +
                "                  </tbody>\n" +
                "                </table>\n" +
                "\n" +
                "            <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"24\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "            <table class=\"Section DataBlocks\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;width: 100%;\">\n" +
                "              <tbody>\n" +
                "              <tr>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                <td class=\"Content\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;\">\n" +
                "                  <table class=\"DataBlocks DataBlocks--three\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;width: 100%;\">\n" +
                "                    <tbody>\n" +
                "                    <tr>\n" +
                "                      \n" +
                "                        <td class=\"Spacer DataBlocks-spacer\" width=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                        <td class=\"DataBlocks-item\" valign=\"top\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;\">\n" +
                "                          <table style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;\">\n" +
                "                            <tbody>\n" +
                "                            <tr>\n" +
                "                              <td class=\"Font Font--caption Font--uppercase Font--mute Font--noWrap\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #8898aa;font-size: 12px;line-height: 16px;white-space: nowrap;font-weight: bold;text-transform: uppercase;\">\n" +
                "                                  Date paid\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                              <td class=\"Font Font--body Font--noWrap\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;white-space: nowrap;\">\n" +
                "                                    "+date+"\n" +
                "                              </td>\n" +
                "                            </tr>\n" +
                "                            </tbody>\n" +
                "                          </table>\n" +
                "                        </td>\n" +
                "\n" +
                "                          <td class=\"Spacer DataBlocks-spacer\" width=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          <td class=\"DataBlocks-item\" valign=\"top\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;\">\n" +
                "                            <table style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;\">\n" +
                "                              <tbody>\n" +
                "                              <tr>\n" +
                "                                <td class=\"Font Font--caption Font--uppercase Font--mute Font--noWrap\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #8898aa;font-size: 12px;line-height: 16px;white-space: nowrap;font-weight: bold;text-transform: uppercase;\">\n" +
                "                                    Payment method\n" +
                "                                </td>\n" +
                "                              </tr>\n" +
                "                              <tr>\n" +
                "                                <td class=\"Font Font--body Font--noWrap\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;white-space: nowrap;\">\n" +
                "                                    \n" +
                "                                  <span> Cash on Delivery</span>\n" +
                "                                </td>\n" +
                "                              </tr>\n" +
                "                              </tbody>\n" +
                "                            </table>\n" +
                "                          </td>\n" +
                "                    </tr>\n" +
                "                    </tbody>\n" +
                "                  </table>\n" +
                "                </td>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "              </tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "\n" +
                "            <table class=\"Section Divider\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\"><tbody><tr><td class=\"Spacer Spacer--divider\" height=\"32\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr></tbody></table>\n" +
                "\n" +
                "\n" +
                "\n" +
                "            <table class=\"Section Copy\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "              <tbody>\n" +
                "              <tr>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                <td class=\"Content Font Font--caption Font--uppercase Font--mute Delink\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #8898aa;font-size: 12px;line-height: 16px;font-weight: bold;text-transform: uppercase;\">\n" +
                "                  Summary\n" +
                "                </td>\n" +
                "                <td class=\"Spacer Spacer--gutter\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "              </tr>\n" +
                "              <tr><td class=\"Spacer Spacer--divider\" colspan=\"3\" height=\"12\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr>\n" +
                "              </tbody>\n" +
                "            </table>\n" +
                "\n" +
                "            <table class=\"Section Table\" width=\"100%\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;background-color: #ffffff;\">\n" +
                "              <tbody>\n" +
                "              <tr>\n" +
                "                <td class=\"Spacer Spacer--kill\" width=\"64\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                <td class=\"Content\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 472px;\">\n" +
                "                  <table class=\"Table-body\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;width: 100%;background-color: #f6f9fc;border-radius: 4px;\">\n" +
                "                    <tbody>\n" +
                "                    <tr><td class=\"Spacer Spacer--divider\" colspan=\"3\" height=\"4\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td></tr>\n" +
                "                    <tr>\n" +
                "                      <td class=\"Spacer Spacer--gutter\" width=\"20\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                      <td class=\"Table-content\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;width: 432px;\">\n" +
                "                        <table class=\"Table-rows\" width=\"432\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;\">\n" +
                "                          <tbody>\n" +
                "                          <tr>\n" +
                "                            <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                          </tr>\n" +
                "                              <tr>\n" +
                "                                <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                              </tr>\n" +
                "\n" +
                "                              \n" +
                "                              <tr>\n" +
                "                                <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                              </tr>\n" +
                "                              <tr>\n" +
                "                                <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                              </tr>";
        return html;
    }

    private String addProductToHTMl(PurchasedItem purchasedItem) {
        ProductDTO productDTO = getProduct(purchasedItem.getProductId());
        String productHtml = "<tr>\n" +
                "                                  <td class=\"Table-description Font Font--body\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;\">\n" +
                "                                    <div style=\"\">\n" +
                "                                      "+productDTO.getProductName()+"\n" +
                "                                      <span class=\"Content Font Font--mute Delink\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;color: #8898aa;font-size: 14px;line-height: 14px;\">\n" +
                "                                        × "+ purchasedItem.getQuantity() +"\n" +
                "                                      </span>\n" +
                "                                    </div>\n" +
                "                                  </td>\n" +
                "                                  <td class=\"Spacer Table-gap\" width=\"8\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                                  <td class=\"Table-amount Font Font--body\" align=\"right\" valign=\"top\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;mso-line-height-rule: exactly;vertical-align: middle;color: #525f7f;font-size: 15px;line-height: 24px;\">\n" +
                "                                    ₹ "+purchasedItem.getPrice()+"\n" +
                "                                  </td>\n" +
                "                                </tr>\n" +
                "\n" +
                "                              <tr>\n" +
                "                                <td class=\"Table-divider Spacer\" colspan=\"3\" height=\"6\" style=\"border: 0;border-collapse: collapse;margin: 0;padding: 0;-webkit-font-smoothing: antialiased;-moz-osx-font-smoothing: grayscale;color: #ffffff;font-size: 1px;line-height: 1px;mso-line-height-rule: exactly;\">&nbsp;</td>\n" +
                "                              </tr>";
        return productHtml;
    }


    @Override
    public UserOrder findOne(String orderID) {
        return userOrderRepository.findOne(orderID);
    }

    @Override
    public List<UserPurchasedItemDTO> findByUserId(String userId) {

        List<UserPurchasedItemDTO> userPurchasedItemDTOS = new ArrayList<>();

        for (UserOrder userOrder : userOrderRepository.findByUserId(userId)) {

            System.out.println(userOrder.getUserOrderId());
            UserPurchasedItemDTO temp = new UserPurchasedItemDTO();
            temp.setUserOrder(userOrder);
            List<PurchasedItem> purchasedItemList = purchasedItemRepository.findByUserOrderId(userOrder.getUserOrderId());
            List<ProductDTO> productDTOList = new ArrayList<>();
            for (PurchasedItem purchasedItem : purchasedItemList) {
                ProductDTO productDTO = getProduct(purchasedItem.getProductId());
                productDTO.setQuantity(purchasedItem.getQuantity());

                productDTOList.add(productDTO);
            }
            temp.setProductDTOList(productDTOList);
            userPurchasedItemDTOS.add(temp);
        }
        return userPurchasedItemDTOS;
    }

    @Override
    public RecieptDTO findByOrderId(String orderId) {

        RecieptDTO recieptDTO = new RecieptDTO();
        UserOrder userOrder = userOrderRepository.findOne(orderId);
        recieptDTO.setUserOrderData(userOrder);
        List<RecieptProductDTO> recieptProductDTOList = new ArrayList<>();
        for (PurchasedItem purchasedItem : userOrder.getPurchasedItemList()) {
            RecieptProductDTO recieptProductDTO = new RecieptProductDTO();
            ProductDTO productDTO = getProduct(purchasedItem.getProductId());
            MerchantDTO merchantDTO = getMerchant(purchasedItem.getMerchantId());
            recieptProductDTO.setProductData(productDTO);
            recieptProductDTO.setMerchantData(merchantDTO);
            recieptProductDTO.setRating(getRating(purchasedItem.getUserOrderId().getUserOrderId(),purchasedItem.getProductId(), userOrder.getUserId()));
            recieptProductDTO.setPrice(Double.valueOf(purchasedItem.getPrice()));
            recieptProductDTO.setQuantity(purchasedItem.getQuantity());
            recieptProductDTOList.add(recieptProductDTO);
        }
        recieptDTO.setRecieptProductDTOList(recieptProductDTOList);
        return recieptDTO;
    }

    private Double getRating(String orderId, String productId, String userId) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(productId);
        ProductRatingDTO productRatingDTO = new ProductRatingDTO();
        productRatingDTO.setOrderId(orderId);
        productRatingDTO.setProductId(productId);
        productRatingDTO.setUserId(userId);
        String URL = EndPoints.BASE_URL + EndPoints.GET_USER_PRODUCT_RATING;
        return restTemplate.postForObject(URL,productRatingDTO, Double.class);
    }

    private MerchantDTO getMerchant(String merchantId) {

        RestTemplate restTemplate = new RestTemplate();
        System.out.println(merchantId);
        MerchantDTO result = restTemplate.getForObject(com.tecknobli.order.merchantmicroservice.Endpoints.BASE_URL + com.tecknobli.order.merchantmicroservice.Endpoints.SINGLE_MERCHANT_URL + merchantId, MerchantDTO.class);
        return result;
    }

    public ProductDTO getProduct(String productId) {
        RestTemplate restTemplate = new RestTemplate();
        System.out.println(productId);
        ProductDTO result = restTemplate.getForObject(Endpoints.BASE_URL + Endpoints.SINGLRPRODUCT_URL + productId, ProductDTO.class);
        if (result != null) {
            return result;
        }
        return null;
    }


}

package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Result;
import com.offcn.group.Cart;
import com.offcn.pojo.TbOrderItem;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    /**
     * 购物车列表
     * @return
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){

        //得到登陆人账号,判断当前是否有人登陆
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //不管登录与否都需要取cookie的购物车信息
        //未登录，页面需要cookie的购物车列表
        //已登录，往redis同不需要cookie的购物车数据
        String cartListString = CookieUtil.getCookieValue(request,"cartList","utf-8");
        if (cartListString==null || "".equals(cartListString)){
            cartListString = "[]";
        }

        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);

        if ("anonymousUser".equals(username)){
            return cartList_cookie;
        }
        //已登陆
        else {
            List<Cart> redis_cartList = cartService.findCartListFromRedis(username);

            ///将cookie的购物车列表同步到redis中
            if (cartList_cookie.size()>0){
                for (Cart cart : cartList_cookie) {
                    for (TbOrderItem tbOrderItem : cart.getOrderItemList()) {
                        redis_cartList = cartService.addGoodsToCartList(redis_cartList, tbOrderItem.getItemId(), tbOrderItem.getNum());
                    }
                }
                // 清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                // 将合并后的数据存入redis
                cartService.saveCartListToRedis(username, redis_cartList);

                System.out.println("merge cookie he reids he bing le");
            }
            return redis_cartList;
        }

    }


    @RequestMapping("/addCart")
    public Result addCart(Long skuId,Integer num){
        try {

            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9009");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // 获取购物车列表
            List<Cart> oldCartList = findCartList();
            List<Cart> newCartList = cartService.addGoodsToCartList(oldCartList, skuId, num);

            //得到登陆人账号,判断当前是否有人登陆
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            if ("anonymousUser".equals(username)){
                CookieUtil.setCookie(request,response,"cartList",JSON.toJSONString(newCartList),7*3600 * 24,"utf-8");
                System.out.println("向cookie存入购物车");
            }
            //已经登录
            else {
                cartService.saveCartListToRedis(username,newCartList);
                System.out.println("向Redis存入购物车");

            }

            //购物车信息放入cookie中
            return new Result(true,"添加购物车成功");
        }catch (Exception e){
            e.printStackTrace();
            return new Result(false,e.getMessage());
        }
    }





}

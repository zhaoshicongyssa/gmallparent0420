package com.atguigu.gmall.product.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @author mqx
 * @date 2020-9-28 10:47:08
 */
public interface ManageService {

    // 查询所有的一级分类数据
    List<BaseCategory1> getCategory1();

    // 根据一级分类Id 查询二级分类数据
    List<BaseCategory2> getCategory2(Long category1Id);

    // 根据二级分类Id 查询三级分类数据
    List<BaseCategory3> getCategory3(Long category2Id);

    // 根据一级分类Id，二级分类Id，三级分类Id 查询平台属性数据
    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    // 保存平台属性接口：
    void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo);

    // 根据平台属性Id 查询平台属性值集合
    List<BaseAttrValue> getAttrValueList(Long attrId);

    // 根据平台属性Id 获取平台属性对象
    BaseAttrInfo getAttrInfo(Long attrId);

    // 根据三级分类Id获取spuInfo 集合
    // (Long category3Id) | (SpuInfo spuInfo)
    // List<SpuInfo> getSpuInfoPage1(Page<SpuInfo> pageParam, Long category3Id);
    // List<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam,SpuInfo spuInfo);

    // 返回IPage 接口
    IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo);


    // 查询所有的销售属性数据
    List<BaseSaleAttr> getBaseSaleAttrList();


    // 保存spuInfo
    void saveSpuInfo(SpuInfo spuInfo);

    // 根据spuId 查询商品图片列表
    List<SpuImage> getSpuImagelist(Long spuId);

    // 根据spuId 查询销售属性对象集合
    List<SpuSaleAttr> getSpuSaleAttrList(Long spuId);

    // 保存skuInfo
    void saveSkuInfo(SkuInfo skuInfo);

    // 查询skuInfo 列表
    IPage<SkuInfo> getPage(Page<SkuInfo> pageParam);

    // 商品上架
    void onSale(Long skuId);
    // 商品下架
    void cancelSale(Long skuId);

    // 根据skuId 获取skuInfo 数据
    SkuInfo getSkuInfo(Long skuId);

    // 通过三级分类Id 查询分类对象视图
    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    // 根据skuId 获取商品的价格
    BigDecimal getSkuPrice(Long skuId);

    // 根据skuId，spuId 查询销售属性数据
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId);

    /**
     * 根据spuId 查询销售属性值Id 与skuId 组合成的map 数据。
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(Long spuId);

    // 查询Json集合
    List<JSONObject> getBaseCategoryList();

    // 根据品牌Id 查询品牌对象
    BaseTrademark getTrademarkByTmId(Long tmId);

    // 根据skuId 查询平台属性
    List<BaseAttrInfo> getAttrList(Long skuId);
}

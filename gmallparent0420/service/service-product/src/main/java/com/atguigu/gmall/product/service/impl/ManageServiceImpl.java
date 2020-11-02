package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author mqx
 * @date 2020-9-28 10:51:56
 */
@Service
public class ManageServiceImpl implements ManageService {

    // 服务层必须调用mapper层
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    private SpuInfoMapper spuInfoMapper;

    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;

    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;

    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;

    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Override
    public List<BaseCategory1> getCategory1() {
        // select * from base_category1;
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        // select * from base_category2 where category1_id = category1Id;
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id",category1Id);
        return baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        // select * from base_category3 where category2_id = category2Id;
        return baseCategory3Mapper.selectList(new QueryWrapper<BaseCategory3>().eq("category2_id",category2Id));
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        /*
        1.  判断用户传入的参数到底是谁?
        2.  base_attr_info 平台属性，base_attr_value 平台属性值
        3.  目前功能，跟平台属性值没有任何关系！根据分类Id 查询平台属性。
        4.  但是，后面有个功能，例如：根据分类Id 查询到平台属性，平台属性值。
        5.  多表关联查询。
         */
        return baseAttrInfoMapper.selectBaseAttrInfoList(category1Id,category2Id,category3Id);
    }

    @Override
    @Transactional
    public void saveBaseAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 既有保存，又有修改！
        // base_attr_info ， base_attr_value
        if (baseAttrInfo.getId()==null){
            baseAttrInfoMapper.insert(baseAttrInfo);
        }else {
            // 修改，平台属性修改的内容只有name
            // baseAttrInfoMapper.update();
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }

        // 如何操作base_attr_value 程序不能确定具体修改的哪个数据，那么应该怎么办?
        // 先将数据统统删除 根据attrId ！然后新增！
        // delete from base_attr_value where attr_id = 1;
        // baseAttrValue.attr_id = baseAttrInfo.id
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueQueryWrapper);

        // 页面会将平台属性值 封装到 attrValueList 对象中 ，
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // String  ()，array ,List size(), File ()
        if (attrValueList!=null && attrValueList.size()>0){
            // iter
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 坑！ 页面在提交数据的时候，并未提交 attrId
                // baseAttrValue.attr_id = baseAttrInfo.id
                // 页面传递的时候，baseAttrInfo 没有执行插入之前，id = null. 执行完成insert 之后 id 不为null
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
        // 坑！ 事务！保证数据的一致性！
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {
        // select * from base_attr_value where attr_id = attrId;
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id",attrId);
        return baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);
    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        // select * from base_attr_info where id = attrId;
        // attrValueList = select * from base_attr_value where attr_id = attrId;
        // baseAttrInfo.setAttrValueList(attrValueList);
        // attrId = baseAttrInfo.getId();
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        // 给平台属性值集合赋值
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));

        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        // 设置查询条件
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.eq("category3_id",spuInfo.getCategory3Id());
        // 设置排序规则 ,mysql 默认排序方式
        spuInfoQueryWrapper.orderByDesc("id");
        //IPage<SpuInfo> spuInfoIPage = spuInfoMapper.selectPage(pageParam, spuInfoQueryWrapper);
        return spuInfoMapper.selectPage(pageParam,spuInfoQueryWrapper);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        // select * from base_sale_attr;
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
         实现类
         1. 保存数据，将数据保存到哪？
            spuInfo
            spuImage
            spuSaleAttr
            spuSaleAttrValue
         */
        // spuInfo
        spuInfoMapper.insert(spuInfo);
        // spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (!CollectionUtils.isEmpty(spuImageList)){
            // 循环添加数据
            for (SpuImage spuImage : spuImageList) {
                // 赋值spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }
        //spuSaleAttr
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (!CollectionUtils.isEmpty(spuSaleAttrList)){
            // 循环遍历
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                // 赋值spuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                // spuSaleAttrValue
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (!CollectionUtils.isEmpty(spuSaleAttrValueList)){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        // 赋值spuId
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImagelist(Long spuId) {
        //select * from spu_image where spu_id = spuId;

        return spuImageMapper.selectList(new QueryWrapper<SpuImage>().eq("spu_id",spuId));
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        // 多表关联查询
        // 在服务层中的方法， get ,save ,set ..
        // 在mapper层 ，select ,insert,del,update ..
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectSpuSaleAttrList(spuId);

        return spuSaleAttrList;
    }

    // 咱们这个要保存skuInfo 的数据！
    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        /*
            sku_attr_value
            sku_image
            sku_info
            sku_sale_attr_value
         */
        skuInfoMapper.insert(skuInfo);
        // sku_image
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }

        // sku_attr_value
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        // sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }
    }

    @Override
    public IPage<SkuInfo> getPage(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        return skuInfoMapper.selectPage(pageParam,skuInfoQueryWrapper);
    }

    @Override
    public void onSale(Long skuId) {
        // update sku_info set is_sale=1 where id = skuId;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public void cancelSale(Long skuId) {
        // update sku_info set is_sale=0 where id = skuId;
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    // 业务整合
    @Override
    @GmallCache(prefix = "getSkuInfo:")
    public SkuInfo getSkuInfo(Long skuId) {
        // return getSkuInfoRedisSet(skuId);
        // return getSkuInfoRedisson(skuId);
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisson(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            // 先获取缓存中的数据！
            // hset(key,field,value) field = 对象的数据。
            // redisTemplate.opsForHash() 没有毛病！但是，我不用！
            // String set(key) get (key)
            // 定义存储数据的key 是什么?
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            // 应该序列化，反序列化。RedisConfig 中已经配置好了！
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 判断
            if (skuInfo==null){
                // 说明缓存中没有数据,从数据库中获取数据，并放入缓存！
                // 防止缓存击穿！
                // 声明锁的key = sku:skuId:lock
                String lockkey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                // 获取lock 对象。
                RLock lock = redissonClient.getLock(lockkey);

                // 方式一：lock.lock(); 默认30秒
                // 方式二：lock.lock(10,TimeUnit.SECONDS);
                // 获取数据库中的数据，防止缓存穿透！数据库中根本没有这个数据！
                // 100009082466.html
                boolean res = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, RedisConst.SKULOCK_EXPIRE_PX2, TimeUnit.SECONDS);
                // 表示上锁成功！
                if (res){
                    try {
                        skuInfo = getSkuInfoDB(skuId);
                        if (skuInfo==null){
                            SkuInfo skuInfo1 = new SkuInfo();
                            // 设置过期时间并返回
                            redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                            return skuInfo1;
                        }
                        // 如果不为空，则直接放入缓存！
                        redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);
                        // 解锁
                        // lock.unlock();
                        // 返回数据！
                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        lock.unlock();
                    }
                }else {
                    // 表示没有获取到分布式锁！
                    try {
                        Thread.sleep(1000);
                        return getSkuInfo(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                // 缓存有数据
                return skuInfo;
            }
        } catch (Exception e) {
            // log 日志，记录。最好附加发送信息给运维。
            e.printStackTrace();
        }
        // 数据库兜底。
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedisSet(Long skuId) {
        // 声明对象
        SkuInfo skuInfo = null;
        try {
            // 先获取缓存中的数据！
            // hset(key,field,value) field = 对象的数据。
            // redisTemplate.opsForHash() 没有毛病！但是，我不用！
            // String set(key) get (key)
            // 定义存储数据的key 是什么?
            String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
            // 应该序列化，反序列化。RedisConfig 中已经配置好了！
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            // 判断
            if (skuInfo==null){
                // 说明缓存中没有数据,从数据库中获取数据，并放入缓存！
                // 防止缓存击穿！
                // 声明锁的key = sku:skuId:lock
                String lockkey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
                // 声明一个UUID
                String uuid = UUID.randomUUID().toString();
                // 上锁
                Boolean flag = redisTemplate.opsForValue().setIfAbsent(lockkey, uuid,RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                // 表示上锁成功！
                if (flag){
                    // 获取数据库中的数据，防止缓存穿透！数据库中根本没有这个数据！
                    // 100009082466.html
                    skuInfo = getSkuInfoDB(skuId);
                    if (skuInfo==null){
                        SkuInfo skuInfo1 = new SkuInfo();
                        // 设置过期时间并返回
                        redisTemplate.opsForValue().set(skuKey,skuInfo1,RedisConst.SKUKEY_TEMPORARY_TIMEOUT,TimeUnit.SECONDS);
                        return skuInfo1;
                    }
                    // 如果不为空，则直接放入缓存！
                    redisTemplate.opsForValue().set(skuKey,skuInfo,RedisConst.SKUKEY_TIMEOUT,TimeUnit.SECONDS);

                    // 声明lua 脚本
                    String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                    // java 客户端如何调用，并执行！
                    // RedisScript
                    DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(script);
                    // 设置返回类型 , 默认是Object ，设置成Long 数据类型是因为：lua 脚本。
                    redisScript.setResultType(Long.class);
                    // redis 中，执行lur 脚本。
                    redisTemplate.execute(redisScript, Arrays.asList(lockkey),uuid);
                    // 返回数据！
                    return skuInfo;
                }else{
                    // 没有获取到锁！等待自旋
                    try {
                        Thread.sleep(1000);
                        return getSkuInfo(skuId);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                // 说明缓存有数据
                return skuInfo;
            }
        } catch (Exception e) {
            // log 日志，记录。最好附加发送信息给运维。
            e.printStackTrace();
        }
        // 返回对象
        return getSkuInfoDB(skuId);
    }

    // 提取方法：ctrl+alt+m
    private SkuInfo getSkuInfoDB(Long skuId) {
        // select * from sku_info where id = skuId;
        // 666
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo!=null){
            // skuName,weight,defaultImage等，
            // 通过skuId 查询skuImage 将返回的数据赋值给SkuInfo
            // select * from sku_image where skuId = skuId;
            QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
            skuImageQueryWrapper.eq("sku_id",skuId);
            List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
            // 赋值商品图片集合
            skuInfo.setSkuImageList(skuImageList);
        }
        return skuInfo;
    }

    @Override
    // 利用aop 实现
    @GmallCache(prefix = "getCategoryView:")
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {
        // select * from base_category_view where id = catagory3Id;
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    @GmallCache(prefix = "skuPrice:")
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo!=null){
            return skuInfo.getPrice();
        }
        return new BigDecimal(0);
    }

    @Override
    @GmallCache(prefix = "getSpuSaleAttrListCheckBySku:")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {

        // 自己编写的sql 语句
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);
    }

    @Override
    @GmallCache(prefix = "getSkuValueIdsMap:")
    public Map getSkuValueIdsMap(Long spuId) {
        // 声明对象
        HashMap hashMap = new HashMap();
        // 将sql 语句查询出来的数据放入map
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        // 判断集合不为空
        if (!CollectionUtils.isEmpty(mapList)){
            // 循环遍历
            for (Map map : mapList) {
                // key = values_ids ,value = sku_id
                hashMap.put(map.get("values_ids"),map.get("sku_id"));
            }
        }
         /*
         可以采用这种方法
         class Vo{
            private String valuesIds;
            private Long skuId;
         }

        在某种条件下，可以使用map 来代替VO
        map.put(values_ids,57|59);
        map.get(values_ids);
          */
        // 返回hashMap
        return hashMap;
    }

    @Override
    @GmallCache(prefix = "index:")
    public List<JSONObject> getBaseCategoryList() {
        //  声明一个集合
        List<JSONObject> list = new ArrayList<>();
        //  查询所有分类数据    BaseCategoryView
        List<BaseCategoryView> baseCategoryViewList = baseCategoryViewMapper.selectList(null);
        //  获取到所有的一级分类数据：
        //  按照一级分类Id 进行分组获取到数Map key = category1Id  value = list .
        Map<Long, List<BaseCategoryView>> category1Map = baseCategoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));

        // 声明一个index
        int index = 1;
        // 循环遍历map
        for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
            // 获取一级分类Id
            Long category1Id = entry.getKey();
            // 获取一级分类Id 所有的数据集合
            List<BaseCategoryView> category2List1 = entry.getValue();
            // 声明一个对象记录一级分类数据
            JSONObject  category1 = new JSONObject();
            category1.put("index",index);
            category1.put("categoryId",category1Id);
            //  每个key 都要循环一次，key 后面对应的值category1Name 一样！
            category1.put("categoryName",category2List1.get(0).getCategory1Name());
            //  index迭代！
            index ++;

            //  获取二级分类数据
            Map<Long, List<BaseCategoryView>> category2Map = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));

            // 声明一个集合来存储二级分类对象
            List<JSONObject> category2Child = new ArrayList<>();
            // 循环遍历map 集合
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : category2Map.entrySet()) {
                // 获取二级分类Id
                Long category2Id = entry2.getKey();
                // 获取二级分类Id下，对应的所有数据
                List<BaseCategoryView> category3List = entry2.getValue();
                // 声明一个对象记录二级分类数据
                JSONObject  category2 = new JSONObject();
                category2.put("categoryId",category2Id);
                category2.put("categoryName",category3List.get(0).getCategory2Name());
                //  categoryChild 还没有存入！
                // 将所有的二级分类对象 存储集合
                category2Child.add(category2);
                // 声明三级分类集合数据
                List<JSONObject> category3Child = new ArrayList<>();
                // 获取三级分类数据
                category3List.forEach(baseCategoryView -> {
                    // 声明一个对象记录二级分类数据
                    JSONObject  category3 = new JSONObject();
                    category3.put("categoryId",baseCategoryView.getCategory3Id());
                    category3.put("categoryName",baseCategoryView.getCategory3Name());
                    // 将三级分类数据添加到集合
                    category3Child.add(category3);
                });
                category2.put("categoryChild",category3Child);
            }
            // 给categoryChild   赋值
            category1.put("categoryChild",category2Child);
            //  将category1 存储list
            list.add(category1);
        }
        return list;
    }

    @Override
    public BaseTrademark getTrademarkByTmId(Long tmId) {
        return baseTrademarkMapper.selectById(tmId);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(Long skuId) {
        // 根据skuId 查询平台属性数据
        return baseAttrInfoMapper.selectBaseAttrInfoListBySkuId(skuId);
    }
}

package com.honvay.cola.cloud.tenancy.service.impl;


import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.honvay.cola.cloud.framework.base.service.impl.BaseSerivceImpl;
import com.honvay.cola.cloud.framework.core.constant.CommonConstant;
import com.honvay.cola.cloud.framework.security.utils.SecurityUtils;
import com.honvay.cola.cloud.framework.util.StringUtils;
import com.honvay.cola.cloud.tenancy.entity.SysTenant;
import com.honvay.cola.cloud.tenancy.mapper.SysTenantMapper;
import com.honvay.cola.cloud.tenancy.model.SysTenantVO;
import com.honvay.cola.cloud.tenancy.service.SysMemberService;
import com.honvay.cola.cloud.tenancy.service.SysTenantService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 系统管理-租户管理
 * </p>
 *
 * @author cola
 * @since 2017-12-11
 */
@Service
public class SysTenantServiceImpl extends BaseSerivceImpl<SysTenant> implements SysTenantService {

   /* @Autowired
    private SysUserService sysUserService;*/

    @Autowired
    private SysMemberService sysMemberService;

    /*@Autowired
    private SysOrganizationService sysOrganizationService;

    @Autowired
    private SysEmployeeService sysEmployeeService;*/

    @Override
    public Page<List<SysTenantVO>> getTenantList(Page page, String code, String name){
        SysTenantMapper mapper = this.getMapper(SysTenantMapper.class);
        name = StringUtils.concatLikeAll(name);
        code = StringUtils.concatLikeAll(code);
        List<SysTenantVO> list = mapper.findTenantList(page, code, name);
        page.setRecords(list);
        return page;
    }

    private void assertDuplication(SysTenant sysTenant){
        EntityWrapper<SysTenant> wrapper = this.newEntityWrapper();
        wrapper.eq("code",sysTenant.getCode());
        if(sysTenant.getId() != null){
            wrapper.ne("id",sysTenant.getId());
        }
        Assert.isTrue(CollectionUtils.isEmpty(this.selectList(wrapper)),"租户编号已存在");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SysTenantVO sysTenantVO){
        //创建管理员账号
        /*SysUser sysUser = new SysUser();
        sysUser.setName(sysTenantVO.getName() + "管理员");
        sysUser.setUsername(sysTenantVO.getAdministrator());
        this.sysUserService.insert(sysUser);

        //创建租户
        SysTenant sysTenant = new SysTenant();
        BeanUtils.copyProperties(sysTenantVO,sysTenant);
        sysTenant.setAdministrator(sysUser.getId());
        this.insert(sysTenant);

        //创建成员
        SysMember sysMember = new SysMember();
        sysMember.setTenantId(sysTenant.getId());
        sysMember.setSysUserId(sysUser.getId());
        this.sysMemberService.insert(sysMember);

        //创建组织
        SysOrganization sysOrganization = new SysOrganization();
        sysOrganization.setCode(sysTenant.getCode());
        sysOrganization.setName(sysTenant.getName());
        sysOrganization.setPid(null);
        sysOrganization.setDeleted(CommonConstant.NO);
        sysOrganization.setTenantId(sysTenant.getId());
        sysOrganizationService.insert(sysOrganization);

        //创建员工
        SysEmployee sysEmployee = new SysEmployee();
        sysEmployee.setSysOrgId(sysOrganization.getId());
        sysEmployee.setSysUserId(sysUser.getId());
        sysEmployeeService.insert(sysEmployee);*/

    }

    @Override
    public boolean insert(SysTenant entity) {
        //检查是否重复
        this.assertDuplication(entity);
        entity.setCheckTime(new Date());
        entity.setCreatorId(SecurityUtils.getUserId());
        entity.setStatus(CommonConstant.TENANT_STATUS_ENABLED);
        entity.setDeleted("N");
        return super.insert(entity);
    }

    @Override
    public void update(SysTenantVO sysTenantVO){
        SysTenant sysTenant = this.selectById(sysTenantVO.getId());
        Assert.notNull(sysTenant,"租户不存在");
        BeanUtils.copyProperties(sysTenantVO,sysTenant,"administrator");
        this.assertDuplication(sysTenant);
        this.updateById(sysTenant);
    }


    @Override
    public void delete(Long id){
        SysTenant sysTenant = this.selectById(id);
        Assert.notNull(sysTenant,"租户不存在");
        Assert.isTrue(sysTenant.getDeleted().equals("N"),"租户已删除");
        sysTenant.setDeleted("Y");
        super.updateById(sysTenant);
    }

    @Override
    public SysTenant disable(Long id) {
        SysTenant sysTenant = this.selectById(id);
        Assert.notNull(sysTenant,"租户不存在");
        Assert.isTrue(sysTenant.getStatus().equals(CommonConstant.TENANT_STATUS_ENABLED), "租户已禁用");
        sysTenant.setStatus(CommonConstant.TENANT_STATUS_DISABLE);
        this.updateById(sysTenant);
        return sysTenant;
    }

    @Override
    public SysTenant enable(Long id) {
        SysTenant sysTenant = this.selectById(id);
        Assert.notNull(sysTenant,"租户不存在");
        //判断是否是异常状态
        Assert.isTrue(sysTenant.getStatus().equals(CommonConstant.TENANT_STATUS_DISABLE), "租户已启用");
        sysTenant.setStatus(CommonConstant.TENANT_STATUS_ENABLED);
        this.updateById(sysTenant);
        return sysTenant;
    }

    @Override
    public void setAdministrator(Long tenantId, Long userId){
        SysTenant sysTenant = this.selectById(tenantId);
        sysTenant.setAdministrator(userId);
        super.updateById(sysTenant);
    }
}

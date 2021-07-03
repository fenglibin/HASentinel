package com.alibaba.csp.sentinel.dashboard.controller.v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.dashboard.auth.AuthService;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntityV2;
import com.alibaba.csp.sentinel.dashboard.domain.Result;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.util.StringUtil;

import lombok.extern.slf4j.Slf4j;

/**
 * AuthorityRuleControllerV2
 *
 * @author rodbate
 * @author fenglibin
 * @since 2019/04/19 11:18
 */
@Slf4j
@RestController
@RequestMapping(value = "/v2/authority")
public class AuthorityRuleControllerV2 {

	@Autowired
	private DynamicRulePublisher<List<AuthorityRuleEntity>> rulePublisher;

	@Autowired
	private DynamicRuleProvider<List<AuthorityRuleEntity>> ruleProvider;

	@Autowired
	private RuleRepository<AuthorityRuleEntity, Long> repository;

	@Autowired
	private AuthService<HttpServletRequest> authService;

	/**
	 * query all authority rules for app
	 *
	 * @param request http request
	 * @param app     app
	 * @return <pre>{@code Result<List < AuthorityRuleEntity>>}</pre>
	 */
	@GetMapping("/rules")
	public Result<List<AuthorityRuleEntityV2>> apiQueryAllRulesForApp(HttpServletRequest request,
			@RequestParam String app) {
		AuthService.AuthUser authUser = authService.getAuthUser(request);
		authUser.authTarget(app, AuthService.PrivilegeType.READ_RULE);
		if (StringUtil.isEmpty(app)) {
			return Result.ofFail(-1, "app cannot be null or empty");
		}
		try {
			List<AuthorityRuleEntity> rules = ruleProvider.getRules(app);
			repository.saveAll(rules);
			final List<AuthorityRuleEntityV2> rulesV2 = new ArrayList<AuthorityRuleEntityV2>();
			if (rules != null && rules.size() > 0) {
				rules.forEach(r -> {
					rulesV2.add(r.getAuthorityRuleEntityV2());
				});
			}
			return Result.ofSuccess(rulesV2);
		} catch (Throwable throwable) {
			log.error("Error when querying authority rules", throwable);
			return Result.ofFail(-1, throwable.getMessage());
		}
	}

	private <R> Result<R> checkEntityInternal(AuthorityRuleEntityV2 entity) {
		if (entity == null) {
			return Result.ofFail(-1, "bad rule body");
		}
		if (StringUtil.isBlank(entity.getApp())) {
			return Result.ofFail(-1, "app can't be null or empty");
		}
		if (entity.getRule() == null) {
			return Result.ofFail(-1, "rule can't be null");
		}
		if (StringUtil.isBlank(entity.getResource())) {
			return Result.ofFail(-1, "resource name cannot be null or empty");
		}
		if (StringUtil.isBlank(entity.getLimitApp())) {
			return Result.ofFail(-1, "limitApp should be valid");
		}
		if (entity.getStrategy() != RuleConstant.AUTHORITY_WHITE
				&& entity.getStrategy() != RuleConstant.AUTHORITY_BLACK) {
			return Result.ofFail(-1, "Unknown strategy (must be blacklist or whitelist)");
		}
		return null;
	}

	/**
	 * add authority rule
	 *
	 * @param request request
	 * @param entity  rule entity
	 * @return <pre>{@code Result<AuthorityRuleEntity>}</pre>
	 */
	@PostMapping("/rule")
	public Result<AuthorityRuleEntityV2> apiAddAuthorityRule(HttpServletRequest request,
			@RequestBody AuthorityRuleEntity entity) {
		AuthService.AuthUser authUser = authService.getAuthUser(request);
		authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
		Result<AuthorityRuleEntityV2> checkResult = checkEntityInternal(entity.getAuthorityRuleEntityV2());
		if (checkResult != null) {
			return checkResult;
		}
		entity.setId(null);
		Date date = new Date();
		entity.setGmtCreate(date);
		entity.setGmtModified(date);
		try {
			entity = repository.save(entity);
			publishRules(entity.getApp());
		} catch (Throwable throwable) {
			log.error("Failed to add authority rule", throwable);
			return Result.ofThrowable(-1, throwable);
		}
		return Result.ofSuccess(entity.getAuthorityRuleEntityV2());
	}

	/**
	 * update authority rule
	 *
	 * @param request request
	 * @param id      rule id
	 * @param entity  rule entity
	 * @return <pre>{@code Result<AuthorityRuleEntity>}</pre>
	 */
	@PutMapping("/rule/{id}")
	public Result<AuthorityRuleEntityV2> apiUpdateAuthorityRule(HttpServletRequest request, @PathVariable("id") Long id,
			@RequestBody AuthorityRuleEntity entity) {
		AuthService.AuthUser authUser = authService.getAuthUser(request);
		authUser.authTarget(entity.getApp(), AuthService.PrivilegeType.WRITE_RULE);
		if (id == null || id <= 0) {
			return Result.ofFail(-1, "Invalid id");
		}
		Result<AuthorityRuleEntityV2> checkResult = checkEntityInternal(entity.getAuthorityRuleEntityV2());
		if (checkResult != null) {
			return checkResult;
		}
		entity.setId(id);
		Date date = new Date();
		entity.setGmtCreate(null);
		entity.setGmtModified(date);
		try {
			entity = repository.save(entity);
			if (entity == null) {
				return Result.ofFail(-1, "Failed to save authority rule");
			}
			publishRules(entity.getApp());
		} catch (Throwable throwable) {
			log.error("Failed to save authority rule", throwable);
			return Result.ofThrowable(-1, throwable);
		}
		return Result.ofSuccess(entity.getAuthorityRuleEntityV2());
	}

	/**
	 * delete authority rule
	 *
	 * @param request request
	 * @param id      rule id
	 * @return <pre>{@code Result<Long>}</pre>
	 */
	@DeleteMapping("/rule/{id}")
	public Result<String> apiDeleteRule(HttpServletRequest request, @PathVariable("id") Long id) {
		AuthService.AuthUser authUser = authService.getAuthUser(request);
		if (id == null) {
			return Result.ofFail(-1, "id cannot be null");
		}
		AuthorityRuleEntity oldEntity = repository.findById(id);
		if (oldEntity == null) {
			return Result.ofSuccess(null);
		}
		authUser.authTarget(oldEntity.getApp(), AuthService.PrivilegeType.DELETE_RULE);
		try {
			repository.delete(id);
			publishRules(oldEntity.getApp());
		} catch (Exception e) {
			return Result.ofFail(-1, e.getMessage());
		}
		return Result.ofSuccess(String.valueOf(id));
	}

	private void publishRules(String app) throws Exception {
		List<AuthorityRuleEntity> rules = repository.findAllByApp(app);
		rulePublisher.publish(app, rules);
	}

}

package com.bankshield.api.controller;

import com.bankshield.api.entity.DataAsset;
import com.bankshield.api.entity.ClassificationHistory;
import com.bankshield.api.service.ClassificationService;
import com.bankshield.common.test.BaseIntegrationTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据资产管理API集成测试
 * 测试数据资产发现、分类、审核等完整流程
 *
 * @author BankShield
 */
@DisplayName("数据资产管理API集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DataAssetControllerIntegrationTest extends BaseIntegrationTest {

    private static Long testDataSourceId = 1L;
    private static Long testAssetId;
    private static Long testTaskId;

    @Autowired
    private ClassificationService classificationService;

    @Test
    @Order(1)
    @DisplayName("管理员登录")
    void testAdminLogin() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "admin");
        loginRequest.put("password", "123456");

        Response response = given()
                .contentType(io.restassured.http.ContentType.JSON)
                .body(toJson(loginRequest))
                .when()
                .post("/api/auth/login");

        assertSuccessResponse(response);
        response.then()
                .body("data.accessToken", notNullValue());

        adminToken = response.jsonPath().getString("data.accessToken");
    }

    @Test
    @Order(2)
    @DisplayName("启动资产发现任务-成功")
    void testStartAssetDiscoverySuccess() {
        Map<String, Object> request = new HashMap<>();
        request.put("dataSourceId", testDataSourceId);

        Response response = givenAuthenticated()
                .body(toJson(request))
                .when()
                .post("/api/asset/discover");

        assertSuccessResponse(response);
        response.then()
                .body("data.taskId", notNullValue())
                .body("message", containsString("资产发现任务已启动"));

        testTaskId = response.jsonPath().getLong("data.taskId");
        assertNotNull(testTaskId);
    }

    @Test
    @Order(3)
    @DisplayName("启动资产发现任务-数据源不存在")
    void testStartAssetDiscoveryDataSourceNotFound() {
        Map<String, Object> request = new HashMap<>();
        request.put("dataSourceId", 99999L);

        Response response = givenAuthenticated()
                .body(toJson(request))
                .when()
                .post("/api/asset/discover");

        assertErrorResponse(response, 404);
        response.then()
                .body("message", containsString("数据源不存在"));
    }

    @Test
    @Order(4)
    @DisplayName("查询扫描进度-成功")
    void testGetScanProgressSuccess() {
        if (testTaskId == null) {
            testTaskId = 1L;
        }

        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/scan-progress/" + testTaskId);

        assertSuccessResponse(response);
        response.then()
                .body("data.taskId", equalTo(testTaskId.intValue()));
    }

    @Test
    @Order(5)
    @DisplayName("停止扫描任务-成功")
    void testStopScanTaskSuccess() {
        if (testTaskId == null) {
            testTaskId = 1L;
        }

        Response response = givenAuthenticated()
                .when()
                .post("/api/asset/scan-stop/" + testTaskId);

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("扫描任务已停止"));
    }

    @Test
    @Order(6)
    @DisplayName("查询资产详情-成功")
    void testGetAssetDetailSuccess() {
        testAssetId = 1L;

        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/" + testAssetId);

        assertSuccessResponse(response);
        response.then()
                .body("data.id", equalTo(testAssetId.intValue()))
                .body("data.assetName", notNullValue());
    }

    @Test
    @Order(7)
    @DisplayName("查询资产详情-资产不存在")
    void testGetAssetDetailNotFound() {
        Long nonExistentAssetId = 99999L;

        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/" + nonExistentAssetId);

        assertErrorResponse(response, 404);
        response.then()
                .body("message", containsString("资产不存在"));
    }

    @Test
    @Order(8)
    @DisplayName("人工标注资产-成功")
    void testManualClassifySuccess() {
        if (testAssetId == null) {
            testAssetId = 1L;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("manualLevel", 3);
        request.put("operatorId", 1L);

        Response response = givenAuthenticated()
                .queryParams(request)
                .when()
                .put("/api/asset/" + testAssetId + "/classify");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("人工标注成功"));
    }

    @Test
    @Order(9)
    @DisplayName("提交审核-成功")
    void testSubmitReviewSuccess() {
        if (testAssetId == null) {
            testAssetId = 1L;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("finalLevel", 3);
        request.put("reason", "测试审核");

        Response response = givenAuthenticated()
                .queryParams(request)
                .when()
                .post("/api/asset/" + testAssetId + "/review");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("提交审核成功"));
    }

    @Test
    @Order(10)
    @DisplayName("审核通过-成功")
    void testApproveAssetSuccess() {
        if (testAssetId == null) {
            testAssetId = 1L;
        }

        Map<String, Object> request = new HashMap<>();
        request.put("approved", true);
        request.put("comment", "审核通过");
        request.put("reviewerId", 2L);

        Response response = givenAuthenticated()
                .queryParams(request)
                .when()
                .put("/api/asset/" + testAssetId + "/approve");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("审核完成"));
    }

    @Test
    @Order(11)
    @DisplayName("审核拒绝-成功")
    void testRejectAssetSuccess() {
        Long rejectAssetId = 2L;

        Map<String, Object> request = new HashMap<>();
        request.put("approved", false);
        request.put("comment", "数据不符合规范");
        request.put("reviewerId", 2L);

        Response response = givenAuthenticated()
                .queryParams(request)
                .when()
                .put("/api/asset/" + rejectAssetId + "/approve");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("审核完成"));
    }

    @Test
    @Order(12)
    @DisplayName("分页查询资产列表-成功")
    void testGetAssetPageSuccess() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 1);
        queryParams.put("size", 10);

        Response response = givenAuthenticated()
                .queryParams(queryParams)
                .when()
                .get("/api/asset/list");

        assertSuccessResponse(response);
        assertPageResponse(response, 1);
    }

    @Test
    @Order(13)
    @DisplayName("分页查询资产列表-带筛选条件")
    void testGetAssetPageWithFilters() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 1);
        queryParams.put("size", 10);
        queryParams.put("assetName", "客户");
        queryParams.put("securityLevel", 3);
        queryParams.put("status", 1);

        Response response = givenAuthenticated()
                .queryParams(queryParams)
                .when()
                .get("/api/asset/list");

        assertSuccessResponse(response);
        assertPageResponse(response, 1);
    }

    @Test
    @Order(14)
    @DisplayName("获取资产地图概览-成功")
    void testGetAssetOverviewSuccess() {
        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/map/overview");

        assertSuccessResponse(response);
        response.then()
                .body("data.totalAssets", notNullValue())
                .body("data.classifiedAssets", notNullValue())
                .body("data.businessLineDistribution", notNullValue())
                .body("data.storageDistribution", notNullValue());
    }

    @Test
    @Order(15)
    @DisplayName("资产下钻查询-按业务条线")
    void testDrillDownByBusinessLine() {
        Map<String, Object> params = new HashMap<>();
        params.put("dimension", "businessLine");
        params.put("dimensionValue", "零售银行");
        params.put("page", 1);
        params.put("size", 10);

        Response response = givenAuthenticated()
                .queryParams(params)
                .when()
                .get("/api/asset/map/drill-down");

        assertSuccessResponse(response);
        response.then()
                .body("data.records", notNullValue())
                .body("data.total", notNullValue());
    }

    @Test
    @Order(16)
    @DisplayName("资产下钻查询-按存储位置")
    void testDrillDownByStorage() {
        Map<String, Object> params = new HashMap<>();
        params.put("dimension", "storageLocation");
        params.put("page", 1);
        params.put("size", 10);

        Response response = givenAuthenticated()
                .queryParams(params)
                .when()
                .get("/api/asset/map/drill-down");

        assertSuccessResponse(response);
        assertSuccessResponse(response);
    }

    @Test
    @Order(17)
    @DisplayName("获取待审核资产列表-成功")
    void testGetPendingReviewAssetsSuccess() {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 1);
        queryParams.put("size", 10);

        Response response = givenAuthenticated()
                .queryParams(queryParams)
                .when()
                .get("/api/asset/pending-review");

        assertSuccessResponse(response);
        assertPageResponse(response, 1);
    }

    @Test
    @Order(18)
    @DisplayName("获取风险资产清单-成功")
    void testGetRiskAssetsSuccess() {
        Map<String, Object> params = new HashMap<>();
        params.put("riskLevel", "HIGH");

        Response response = givenAuthenticated()
                .queryParams(params)
                .when()
                .get("/api/asset/risk-assets");

        assertSuccessResponse(response);
        response.then()
                .body("data", notNullValue());
    }

    @Test
    @Order(19)
    @DisplayName("批量审核通过-成功")
    void testBatchApproveAssetsSuccess() {
        Map<String, Object> request = new HashMap<>();
        request.put("assetIds", new Integer[]{1, 2, 3});
        request.put("reviewerId", 2L);

        Response response = givenAuthenticated()
                .body(toJson(request))
                .when()
                .post("/api/asset/batch-approve");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("批量审核完成"));
    }

    @Test
    @Order(20)
    @DisplayName("批量审核拒绝-成功")
    void testBatchRejectAssetsSuccess() {
        Map<String, Object> request = new HashMap<>();
        request.put("assetIds", new Integer[]{4, 5, 6});
        request.put("comment", "批量审核拒绝");
        request.put("reviewerId", 2L);

        Response response = givenAuthenticated()
                .body(toJson(request))
                .when()
                .post("/api/asset/batch-reject");

        assertSuccessResponse(response);
        response.then()
                .body("message", containsString("批量审核完成"));
    }

    @Test
    @Order(21)
    @DisplayName("获取业务条线分布-成功")
    void testGetBusinessLineDistributionSuccess() {
        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/business-line-distribution");

        assertSuccessResponse(response);
        response.then()
                .body("data", notNullValue());
    }

    @Test
    @Order(22)
    @DisplayName("获取存储位置分布-成功")
    void testGetStorageDistributionSuccess() {
        Response response = givenAuthenticated()
                .when()
                .get("/api/asset/storage-distribution");

        assertSuccessResponse(response);
        response.then()
                .body("data", notNullValue());
    }

    @Test
    @Order(23)
    @DisplayName("导出资产清单-Excel格式")
    void testExportAssetListExcel() {
        Map<String, Object> params = new HashMap<>();
        params.put("exportType", "EXCEL");

        Response response = givenAuthenticated()
                .queryParams(params)
                .when()
                .get("/api/asset/export");

        assertSuccessResponse(response);
        response.then()
                .body("data", notNullValue())
                .body("message", containsString("导出成功"));
    }

    @Test
    @Order(24)
    @DisplayName("导出资产清单-PDF格式")
    void testExportAssetListPDF() {
        Map<String, Object> params = new HashMap<>();
        params.put("exportType", "PDF");

        Response response = givenAuthenticated()
                .queryParams(params)
                .when()
                .get("/api/asset/export");

        assertSuccessResponse(response);
        response.then()
                .body("data", notNullValue());
    }

    @Test
    @Order(25)
    @DisplayName("资产管理API性能测试")
    void testAssetManagementPerformance() {
        long startTime = System.currentTimeMillis();

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("page", 1);
        queryParams.put("size", 10);

        Response response = givenAuthenticated()
                .queryParams(queryParams)
                .when()
                .get("/api/asset/list");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertSuccessResponse(response);
        assertTrue(duration < 2000, "API响应时间应小于2秒");
    }
}

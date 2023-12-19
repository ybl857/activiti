package com.example.bpmnactiviti;

import com.example.bpmnactiviti.service.TestServiceImpl;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
class BpmnActivitiApplicationTests {

    @Autowired
    private TestServiceImpl testServiceImpl;

    @Test
    void contextLoads() {
        testServiceImpl.sayHello();
    }

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Test
    void deployProcessDefinition() throws IOException {
        String processDefinitionFilePath = "E:\\work\\test\\bpmn-activiti\\src\\main\\resources\\bpmn\\vacationProcess.bpmn20.xml";
        Deployment deploy = this.repositoryService.createDeployment()
                .addInputStream(processDefinitionFilePath, Files.newInputStream(Paths.get(processDefinitionFilePath)))
                .deploy();
        System.out.println("部署流程定义成功："+ deploy);
    }

    @Test
    void startProcessInstance() {

        //启动流程时传递的参数列表 这里根据实际情况 也可以选择不传
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("姓名", "张三");
        variables.put("请假天数", 4);
        variables.put("请假原因", "我很累！");

        // 根据流程定义ID查询流程定义  leave:1:10004是我们刚才部署的流程定义的id
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId("leave:2:7503")
                .singleResult();

        // 获取流程定义的Key
        String processDefinitionKey = processDefinition.getKey();

        //定义businessKey  businessKey一般为流程实例key与实际业务数据的结合
        //假设一个请假的业务 在数据库中的id是1001
        String businessKey = processDefinitionKey + ":" + "1001";
        //设置启动流程的人
        Authentication.setAuthenticatedUserId("xxyin");
        ProcessInstance processInstance = this.runtimeService
                .startProcessInstanceByKey(processDefinitionKey, businessKey, variables);
        System.out.println("流程启动成功：" + processInstance);
    }

    @Test
    void findTodoTask() {
        TaskQuery taskQuery = taskService.createTaskQuery().orderByTaskCreateTime().asc();
        // <userTask id="deptLeaderVerify" name="部门领导审批" activiti:assignee="zhangsan" ></userTask>
        //添加查询条件 查询指派给 zhangsan 的任务  假设这个任务指派给了zhangsan
        // taskQuery.taskId("12506");
        taskQuery.taskInvolvedUser("zhangsan");
        // taskQuery.task
        //添加查询条件 查询流程定义key为 leave 的任务
        taskQuery.processDefinitionKey("leave");
        List<Task> tasks = taskQuery.list();
        // 处理查询结果
        for (Task task : tasks) {
            System.out.println("Task ID: " + task.getId());
            System.out.println("Task Name: " + task.getName());
            // 其他任务属性的获取和处理
        }
    }

    @Test
    void complete() {
        //这里模拟传进来的参数
        String taskId = "20004";
        String assignee = "xxyin";
        Map<String, Object> variables = new HashMap<>();
        variables.put("pass",true);
        variables.put("comment","销假结束");
        // variables.put("assignee", "xxyin");

        //根据任务id获取到当前的任务
        TaskQuery query = this.taskService.createTaskQuery();
        Task task = query.taskId(taskId).singleResult();

        //将参数列表的评论赋值给comment
        String taskComment = variables.remove("comment").toString();
        //向特定的任务添加评论
        if (!taskComment.equals("")) {
            taskService.addComment(taskId, task.getProcessInstanceId(), taskComment);
        }
        //如果当前任务没有指派人，需要先使用 claim() 方法领取任务
        taskService.claim(taskId,assignee);
        //如果有指派人，直接完成任务
        taskService.complete(taskId,variables);
    }
}

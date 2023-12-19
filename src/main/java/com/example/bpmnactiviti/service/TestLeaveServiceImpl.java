package com.example.bpmnactiviti.service;

import com.example.bpmnactiviti.domain.VacTask;
import com.example.bpmnactiviti.domain.Vacation;
import com.example.bpmnactiviti.util.ActivitiUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @Author yanbl
 * @Date 2023/12/19 14:26
 * @Description ...
 */
@Service
@RequiredArgsConstructor
public class TestLeaveServiceImpl {

    private static final String PROCESS_DEFINE_KEY = "vacationProcess";
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;

    public Object startVac(String userName, Vacation vac) {
        Authentication.setAuthenticatedUserId(userName);
        // 开始流程
        ProcessInstance vacationInstance = runtimeService.startProcessInstanceByKey(PROCESS_DEFINE_KEY);
        // 查询当前任务
        Task currentTask = taskService.createTaskQuery().processInstanceId(vacationInstance.getId()).singleResult();
        // 申明任务
        taskService.claim(currentTask.getId(), userName);

        Map<String, Object> vars = new HashMap<>(4);
        vars.put("applyUser", userName);
        vars.put("days", vac.getDays());
        vars.put("reason", vac.getReason());
        // 完成任务
        taskService.complete(currentTask.getId(), vars);

        return true;
    }

    public Object myAudit(String userName) {
        List<Task> taskList = taskService.createTaskQuery().taskCandidateUser(userName)
                .orderByTaskCreateTime().desc().list();
//        / 多此一举 taskList中包含了以下内容(用户的任务中包含了所在用户组的任务)
//        Group group = identityService.createGroupQuery().groupMember(userName).singleResult();
//        List<Task> list = taskService.createTaskQuery().taskCandidateGroup(group.getId()).list();
//        taskList.addAll(list);
        List<VacTask> vacTaskList = new ArrayList<>();
        for (Task task : taskList) {
            VacTask vacTask = new VacTask();
            vacTask.setId(task.getId());
            vacTask.setName(task.getName());
            vacTask.setCreateTime(task.getCreateTime());
            String instanceId = task.getProcessInstanceId();
            ProcessInstance instance = runtimeService.createProcessInstanceQuery().processInstanceId(instanceId).singleResult();
            Vacation vac = getVac(instance);
            vacTask.setVac(vac);
            vacTaskList.add(vacTask);
        }
        return vacTaskList;
    }

    private Vacation getVac(ProcessInstance instance) {
        Integer days = runtimeService.getVariable(instance.getId(), "days", Integer.class);
        String reason = runtimeService.getVariable(instance.getId(), "reason", String.class);
        Vacation vac = new Vacation();
        vac.setApplyUser(instance.getStartUserId());
        vac.setDays(days);
        vac.setReason(reason);
        Date startTime = instance.getStartTime(); // activiti 6 才有
        vac.setApplyTime(startTime);
        vac.setApplyStatus(instance.isEnded() ? "申请结束" : "等待审批");
        return vac;
    }

    /**
     * 审批请假
     * @param userName
     * @param vacTask
     * @return
     */
    public Object passAudit(String userName, VacTask vacTask) {
        String taskId = vacTask.getId();
        String result = vacTask.getVac().getResult();
        Map<String, Object> vars = new HashMap<>();
        vars.put("result", result);
        vars.put("auditor", userName);
        vars.put("auditTime", new Date());
        taskService.claim(taskId, userName);
        taskService.complete(taskId, vars);
        return true;
    }

    /**
     * 查询请假记录
     * @param userName
     * @return
     */
    public Object myVacRecord(String userName) {
        List<HistoricProcessInstance> hisProInstance = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey(PROCESS_DEFINE_KEY).startedBy(userName).finished()
                .orderByProcessInstanceEndTime().desc().list();

        List<Vacation> vacList = new ArrayList<>();
        for (HistoricProcessInstance hisInstance : hisProInstance) {
            Vacation vacation = new Vacation();
            vacation.setApplyUser(hisInstance.getStartUserId());
            vacation.setApplyTime(hisInstance.getStartTime());
            vacation.setApplyStatus("申请结束");
            List<HistoricVariableInstance> varInstanceList = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(hisInstance.getId()).list();
            ActivitiUtil.setVars(vacation, varInstanceList);
            vacList.add(vacation);
        }
        return vacList;
    }
}

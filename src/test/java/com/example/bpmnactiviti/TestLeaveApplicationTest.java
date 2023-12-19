package com.example.bpmnactiviti;

import com.example.bpmnactiviti.domain.VacTask;
import com.example.bpmnactiviti.domain.Vacation;
import com.example.bpmnactiviti.service.TestLeaveServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @Author yanbl
 * @Date 2023/12/19 14:35
 * @Description ...
 */
@SpringBootTest
public class TestLeaveApplicationTest {

    @Autowired
    private TestLeaveServiceImpl testLeaveServiceImpl;

    @Test
    void testStartVac() {
        Vacation vac = new Vacation();
        vac.setDays(4);
        vac.setReason("感染流感,发热");
        Object o = testLeaveServiceImpl.startVac("maliu", vac);
        System.out.println(o);
    }
    @Test
    void testMyAudit() {
        Object o = testLeaveServiceImpl.myAudit("admin");
        System.out.println(o);
    }

    @Test
    void testPassAudit() {
        Vacation vac = new Vacation();
        vac.setResult("false");
        VacTask vat = new VacTask();
        vat.setVac(vac);
        vat.setId("2511");
        System.out.println(testLeaveServiceImpl.passAudit("admin", vat));
    }
}

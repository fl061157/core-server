package cn.v5.service;

import cn.v5.entity.PhoneBook;
import cn.v5.entity.PhoneKey;
import cn.v5.test.TestTemplate;
import org.junit.Test;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;


public class PhoneBookServiceTest extends TestTemplate {
    @Inject
    private PhoneBookService phoneBookService;


    @Test
    public void testFindById() {
        PhoneBook pb = new PhoneBook();
        PhoneKey pk = new PhoneKey("0086", "123", "123");

        pb.setId(pk);
        manager.insert(pb);

        PhoneBook result = phoneBookService.findUserPhoneBook("0086", "123", "123");

        assertThat(result).isNotNull();
        assertThat(result.getId().getCountryCode()).isEqualTo("0086");
    }
}


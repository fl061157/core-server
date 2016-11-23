package cn.v5.service;

import cn.v5.entity.PhoneBook;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FriendServiceTest2 {

    @Test
    public void testSort() {
        List<PhoneBook> phoneBookList = new ArrayList();

        PhoneBook phoneBook1 = new PhoneBook();
        phoneBook1.setCreateTime(113343L);

        phoneBookList.add(phoneBook1);

        PhoneBook phoneBook2 = new PhoneBook();
        phoneBook2.setCreateTime(114343L);
        phoneBookList.add(phoneBook2);


        PhoneBook phoneBook3 = new PhoneBook();
        phoneBook3.setCreateTime(104343L);
        phoneBookList.add(phoneBook3);

        phoneBookList.sort((o1, o2) -> o1.getCreateTime() - o2.getCreateTime() > 0 ? -1 : 1);


        System.out.println(phoneBookList);


        Assert.assertEquals(phoneBook2, phoneBookList.get(0));
        Assert.assertEquals(phoneBook1, phoneBookList.get(1));
        Assert.assertEquals(phoneBook3, phoneBookList.get(2));

    }

    @Test
    public void testListSlice() {
        int start = 2;
        int end = 3;
        List<String> srcList = new ArrayList<>();
        srcList.add("111");
        Assert.assertTrue(null == getSubList(srcList, start, end));
        srcList.add("222");
        Assert.assertTrue(null == getSubList(srcList, start, end));
        srcList.add("3333");
        Assert.assertTrue(null != getSubList(srcList, start, end));
        Assert.assertEquals(1,getSubList(srcList,start,end).size());
        Assert.assertEquals("3333",getSubList(srcList,start,end).get(0));
//        srcList.add("4444");
//        Assert.assertTrue(null != getSubList(srcList, start, end));
//        Assert.assertEquals(2,getSubList(srcList,start,end).size());
//        Assert.assertEquals("3333",getSubList(srcList,start,end).get(0));
//        Assert.assertEquals("4444",getSubList(srcList,start,end).get(1));


    }

    private List<String> getSubList(List<String> srcList, int start, int end) {
        List result = null;
        if (srcList.size() > start) {
            end = end < srcList.size() ? end : srcList.size();
            result = srcList.subList(start, end);
        }
        return result;
    }

}
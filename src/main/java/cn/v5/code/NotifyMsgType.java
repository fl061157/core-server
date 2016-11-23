package cn.v5.code;

/**
 * Created by hi on 14-4-15.
 */
public interface NotifyMsgType {


    String USER_REGISTER = "user_register";
    String SECRETARY_NOTIFY = "secretary_notify";
    String GROUP_UPDATE = "group_update";
    String GROUP_REMOVEUSER = "group_removeuser";
    String GROUP_EXIT = "group_exit";
    String GROUP_DISMISS = "group_dismiss";

    String GROUP_CREATE = "group_create";
    String GROUP_ADDUSER = "group_adduser";
    String GROUP_APPLY_ADDUSER = "group_apply_adduser";
    String GROUP_APPLY = "group_apply";
    String GROUP_INVITE_AUDIT = "apply_join_group";
    String USER_UPDATE = "user_update";

    String DIGITAL_RADAR_REMOVEUSER = "digital_radar_removeuser";
    String DIGITAL_RADAR_ADDUSER = "digital_radar_adduser";
    String DIGITAL_NOTIFY_FIND_RADAR = "notify_find_radar";

    String FRIEND_CONTACT_REQUEST = "contact_request";
    String FRIEND_CONTACT_REQUEST_SUCCESS = "contact_request_success";

    String COMMAND_PEOPLE_YOU_MAY_KNOWN = "new_people_you_may_known";
}

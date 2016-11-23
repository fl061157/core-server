package cn.v5.bean.ivt;

/**
 * 邀请下载露脸有关页面的多语言信息
 * Created by haoWang on 2016/5/6.
 */
public class InvitePageBean {
    private String title;
    private String account;
    private String invite;
    private String isDownload;
    private String download;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getInvite() {
        return invite;
    }

    public void setInvite(String invite) {
        this.invite = invite;
    }

    public String getIsDownload() {
        return isDownload;
    }

    public void setIsDownload(String isDownload) {
        this.isDownload = isDownload;
    }

    public String getDownload() {
        return download;
    }

    public void setDownload(String download) {
        this.download = download;
    }

    @Override
    public String toString() {
        return "InvitePageBean{" +
                "title='" + title + '\'' +
                ", account='" + account + '\'' +
                ", invite='" + invite + '\'' +
                ", isDownload='" + isDownload + '\'' +
                ", download='" + download + '\'' +
                '}';
    }
}

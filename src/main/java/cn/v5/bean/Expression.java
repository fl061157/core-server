package cn.v5.bean;

/**
 * Created by handwin on 2014/12/2.
 */
public class Expression {
    private String id;
    private String name;
    private String imgUrlAnd;
    private String imgUrlIos;
    private String dataUrl;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImgUrlAnd() {
        return imgUrlAnd;
    }

    public void setImgUrlAnd(String imgUrlAnd) {
        this.imgUrlAnd = imgUrlAnd;
    }

    public String getImgUrlIos() {
        return imgUrlIos;
    }

    public void setImgUrlIos(String imgUrlIos) {
        this.imgUrlIos = imgUrlIos;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public Expression(String id,String name,String imgUrlAnd,String imgUrlIos,String dataUrl){
        this.id=id;
        this.name=name;
        this.imgUrlAnd=imgUrlAnd;
        this.imgUrlIos=imgUrlIos;
        this.dataUrl=dataUrl;
    }

    @Override
    public String toString(){
        return "Expression{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imgUrlAnd='" + imgUrlAnd + '\'' +
                ", imgUrlIos='" + imgUrlIos + '\'' +
                ", dataUrl='" + dataUrl + '\'' +
                '}';
    }

}

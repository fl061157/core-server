package cn.v5.trade;

/**
 * Created by fangliang on 16/9/2.
 */
public abstract class TradeException extends Exception {

    private int code;

    public TradeException(int code, String msg, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    final static int IO_ERROR_CODE = 500;

    final static int INTER_ERROR_CODE = 501;

    final static int TRADE_FAIL_ERROR_CODE = 502;

    final static int PERSIST_ERROR_CODE = 503;

    final static int TRADE_NOT_EXISTS_CODE = 504;

    final static int TRADE_ACCOMPLISH_CODE = 505;


    public static class TradeIOException extends TradeException {

        public TradeIOException(String msg, Throwable throwable) {

            super(IO_ERROR_CODE, msg, throwable);

        }

    }

    public static class TradeInterException extends TradeException {

        public TradeInterException(String msg, Throwable throwable) {

            super(INTER_ERROR_CODE, msg, throwable);

        }
    }

    public static class TradeNotExistsException extends TradeException {
        
        public TradeNotExistsException(String msg) {
            super(TRADE_NOT_EXISTS_CODE, msg, null);
        }
    }

    public static class TradeAccomplishException extends TradeException {

        private String itemID;

        private String tradeNo;

        private boolean success;

        public TradeAccomplishException(String msg) {
            super(TRADE_ACCOMPLISH_CODE, msg, null);
        }


        public TradeAccomplishException buildItemID(String itemID) {
            this.itemID = itemID;
            return this;
        }

        public TradeAccomplishException buildTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
            return this;
        }

        public TradeAccomplishException buildSuccess(boolean success) {
            this.success = success;
            return this;
        }


        public String getItemID() {
            return itemID;
        }

        public String getTradeNo() {
            return tradeNo;
        }

        public boolean isSuccess() {
            return success;
        }
    }


    public static class TradeFailException extends TradeException {

        public TradeFailException(String msg) {

            super(TRADE_FAIL_ERROR_CODE, msg, null);

        }
    }

    public static class TradePersistException extends TradeException {

        public TradePersistException(String msg, Throwable throwable) {

            super(PERSIST_ERROR_CODE, msg, throwable);

        }

    }


}

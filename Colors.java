public enum Colors 
{
    Solution("#09ff00"),
    Discovered("#0006ff"),
    Known("#fff900"),
    Start("#2886ba"),
    Finish("#ba5c28"),
    Empty("#ffffff"),
    Wall("#000000");
 
    private String code;
 
    Colors(String code) {
        this.code = code;
    }
 
    public String getCode() {
        return code;
    }
}
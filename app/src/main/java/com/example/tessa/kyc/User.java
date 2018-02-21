package com.example.tessa.kyc;

import java.util.Date;

/**
 * Created by tessa on 21/2/2018.
 */

public class User {

    public String title;
    public String full_name;
    public String address;
    public String postal_code;
    public String mobile_no;
    public String date_of_birth;
    public String gender;
    public String nationality;
    public String identif_no;
    public String identif_doc_type;
    public String marital_status;
    public Date last_updated;

    public String email;

    public User(String fullName, String postalCode, String identifNo) {
        full_name = fullName;
        postal_code = postalCode;
        identif_no = identifNo;
        //date_of_birth = dob;
    }

}

class Title {
    String[] titles = {"Mr", "Ms", "Dr"};
    String title;
    Title (String title) {
        for (String t:titles) {
            if (title.equalsIgnoreCase(t))
                this.title = title;
        }
    }
}


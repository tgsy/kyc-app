package com.example.tessa.kyc;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tessa on 21/2/2018.
 */

@IgnoreExtraProperties
public class User {

    public String title;
    public String full_name;
    public String address;
    public String postal_code;
    public String mobile_no;
    public String date_of_birth;
    public String gender;
    public String nationality;
    public String id;
    public String id_type;
    public String marital_status;
    public Date last_updated;
    public String email;

    public User(String fullName, String postalCode, String identifNo, String dob) {
        full_name = fullName;
        postal_code = postalCode;
        id = identifNo;
        date_of_birth = dob;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("Full Name", full_name);
        result.put("Postal Code", postal_code);
        result.put("ID No.", id);
        result.put("Date of Birth", date_of_birth);
        return result;
    }
    // [END post_to_map]

}
// [END post_class]




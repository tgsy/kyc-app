package com.example.tessa.kyc.company;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class CompanyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Company> COMPANIES = new ArrayList<Company>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<Integer, Company> COMPANY_MAP = new HashMap<Integer, Company>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addCompany(createCompany(i));
        }
    }

    private static void addCompany(Company company) {
        COMPANIES.add(company);
        COMPANY_MAP.put(company.id, company);
    }

    private static Company createCompany(int position) {
        return new Company(position, "DBS " + position, "UNAUTHORIZED", makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class Company {
        public final int id;
        public final String name;
        public String status;
        public String details;

        public ArrayList<String> statuses = new ArrayList<>(
                Arrays.asList("AUTHORIZED","PENDING AUTHORIZATION", "UNAUTHORIZED"));

        public Company(int id, String name) {
            this.id = id;
            this.name = name;
            this.status = statuses.get(2);
            this.details = null;
        }

        public Company(int id, String name, String status, String details) {
            this.id = id;
            this.name = name;
            if (!statuses.contains(status))
                this.status = statuses.get(2);
            else
                this.status = status.toUpperCase();
            this.details = details;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getStatus() {
            return status;
        }

        public String getDetails() {
            return details;
        }

        public void setStatus(String status) {
            if (!statuses.contains(status))
                this.status = statuses.get(2);
            else
                this.status = status.toUpperCase();
        }

        public void setDetails(String details) {
            this.details = details;
        }

    }
}

package com.costacloud.useractivityservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "user_activity")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoggedActivity {
    @Id
    private String username;
    private List<Activity> activityList;

    public void addActivity(Activity activity) {
        activityList.add(activity);
    }
}

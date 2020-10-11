package heart.your.to.key.pojo;

import java.util.List;

/**
 * @author LiChenke
 **/

public class Consumer {

    private String username;
    private String customId;
    private List<String> tags;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCustomId() {
        return customId;
    }

    public void setCustomId(String customId) {
        this.customId = customId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}

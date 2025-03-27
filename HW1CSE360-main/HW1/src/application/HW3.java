import java.util.*;

/**
 * HW3 class provides functionalities to manage replies and likes in a discussion system.
 */
public class HW3 {
    private final Map<Integer, List<Integer>> nestedReplies = new HashMap<>();
    private final Map<Integer, Set<String>> likeLists = new HashMap<>();
    private final Map<Integer, Integer> likeCounts = new HashMap<>();

    /**
     * Retrieves all nested replies for a given parent reply ID.
     *
     * @param parentReplyId The ID of the parent reply.
     * @param currentUser   The user requesting the replies.
     * @return List of nested reply IDs.
     */
    public List<Integer> getNestedReplies(int parentReplyId, String currentUser) {
        return nestedReplies.getOrDefault(parentReplyId, Collections.emptyList());
    }

    /**
     * Adds a user to the like list of a specific reply.
     *
     * @param userName The user to add.
     * @param replyId  The reply ID to like.
     * @return True if the user was added successfully, false otherwise.
     */
    public boolean addUsertoLikeList(String userName, int replyId) {
        likeLists.putIfAbsent(replyId, new HashSet<>());
        if (likeLists.get(replyId).add(userName)) {
            likeCounts.put(replyId, likeCounts.getOrDefault(replyId, 0) + 1);
            return true;
        }
        return false;
    }

    /**
     * Retrieves the list of users who liked a specific reply.
     *
     * @param replyId The reply ID.
     * @return A set of usernames who liked the reply.
     */
    public Set<String> getLikeList(int replyId) {
        return likeLists.getOrDefault(replyId, Collections.emptySet());
    }

    /**
     * Decreases the like count of a reply, ensuring it does not go below zero.
     *
     * @param replyId The reply ID.
     * @return True if the like count was decremented, false if already zero.
     */
    public boolean decrementLikes(int replyId) {
        if (likeCounts.getOrDefault(replyId, 0) > 0) {
            likeCounts.put(replyId, likeCounts.get(replyId) - 1);
            return true;
        }
        return false;
    }

    /**
     * Removes a user from the like list of a specific reply.
     *
     * @param userName The user to remove.
     * @param replyId  The reply ID.
     * @return True if the user was removed, false if the user was not in the list.
     */
    public boolean removeUserFromLikeList(String userName, int replyId) {
        if (likeLists.containsKey(replyId) && likeLists.get(replyId).remove(userName)) {
            likeCounts.put(replyId, Math.max(0, likeCounts.get(replyId) - 1));
            return true;
        }
        return false;
    }
}

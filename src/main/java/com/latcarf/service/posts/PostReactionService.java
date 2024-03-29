package com.latcarf.service.posts;

import com.latcarf.convert.UserConvert;
import com.latcarf.dto.UserDTO;
import com.latcarf.model.Post;
import com.latcarf.model.User;
import com.latcarf.model.reaction.PostReaction;
import com.latcarf.model.reaction.Reactions;
import com.latcarf.repository.UserRepository;
import com.latcarf.repository.posts.PostReactionRepository;
import com.latcarf.repository.posts.PostRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostReactionService {
    private final PostReactionRepository postReactionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserConvert userConvert;

    public PostReactionService(PostReactionRepository postLikeRepository, UserRepository userRepository, PostRepository postRepository, UserConvert userConvert) {
        this.postReactionRepository = postLikeRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.userConvert = userConvert;
    }

    public void toggleLike(Long postId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new UsernameNotFoundException("Post not found by: " + postId));

        Optional<PostReaction> likeOptional = postReactionRepository.findByTypeAndUserAndPost(Reactions.LIKE.toString(), user, post);

        Optional<PostReaction> dislikeOptional = postReactionRepository.findByTypeAndUserAndPost(Reactions.DISLIKE.toString(), user, post);
        dislikeOptional.ifPresent(postReactionRepository::delete);

        if (likeOptional.isPresent()) {
            postReactionRepository.delete(likeOptional.get());
        } else {
            postReactionRepository.save(new PostReaction(Reactions.LIKE.toString(), user, post));
        }
    }

    public void toggleDislike(Long postId, String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new UsernameNotFoundException("Post not found by: " + postId));

        Optional<PostReaction> dislikeOptional = postReactionRepository.findByTypeAndUserAndPost(Reactions.DISLIKE.toString(), user, post);

        Optional<PostReaction> likeOptional = postReactionRepository.findByTypeAndUserAndPost(Reactions.LIKE.toString(), user, post);
        likeOptional.ifPresent(postReactionRepository::delete);

        if (dislikeOptional.isPresent()) {
            postReactionRepository.delete(dislikeOptional.get());
        } else {
            postReactionRepository.save(new PostReaction(Reactions.DISLIKE.toString(), user, post));
        }
    }

    public Long getLikesCount(Long postId) {
        return postReactionRepository.countByTypeAndPostId(Reactions.LIKE.toString(), postId);
    }

    public Long getDislikesCount(Long postId) {
        return postReactionRepository.countByTypeAndPostId(Reactions.DISLIKE.toString(), postId);
    }

    public List<UserDTO> getAllUserLikes(Long postId) {
        List<PostReaction> reactions = postReactionRepository.findByTypeAndPostId(Reactions.LIKE.toString(), postId);
        return reactions.stream()
                .map(PostReaction::getUser)
                .map(userConvert::convertToUserDTO)
                .toList();
    }

}

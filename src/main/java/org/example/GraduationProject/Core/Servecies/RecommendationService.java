package org.example.GraduationProject.Core.Servecies;

import org.example.GraduationProject.Common.DTOs.RatingDTO;
import org.example.GraduationProject.Common.Entities.Hall;
import org.example.GraduationProject.Common.Entities.User;
import org.example.GraduationProject.Common.Entities.UserHallRatings;

import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Repsitories.HallOwnerRepository;
import org.example.GraduationProject.Core.Repsitories.HallRepository;
import org.example.GraduationProject.Core.Repsitories.UserHallRatingsRepository;
import org.example.GraduationProject.Core.Repsitories.UserRepository;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private UserHallRatingsRepository userHallRatingsRepository;

    @Autowired
    private HallRepository hallRepository;

    @Autowired
    private UserRepository userRepository;


    public List<Hall> recommendHalls(User user) {
        // Get all user-hall ratings
        List<UserHallRatings> userRatings = userHallRatingsRepository.findAll(); // <-- Fetch all user ratings

        // Get target user's ratings
        List<UserHallRatings> targetUserRatings = userRatings.stream() // <-- Stream-based filtering for the target user's ratings
                .filter(rating -> rating.getUser().equals(user))
                .collect(Collectors.toList());

        // If user hasn't rated any halls, return fallback recommendations
        if (targetUserRatings.isEmpty()) {
            return fallbackHallRecommendations(); // <-- Call fallback method if no ratings exist
        }

        // Step 1: Calculate similarity between halls based on ratings
        Map<Hall, Double> hallSimilarities = calculateItemSimilarity(targetUserRatings, userRatings); // <-- Similarity calculation logic added

        // Step 2: Recommend top N halls based on similarity scores
        return hallSimilarities.entrySet().stream() // <-- Stream-based sorting to find top 5 recommendations
                .sorted(Map.Entry.<Hall, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5) // <-- Limit to top 5 recommendations
                .collect(Collectors.toList());
    }

    /**
     * Calculate similarity between halls based on user ratings.
     * This uses cosine similarity between item (hall) ratings.
     * @param targetUserRatings - the ratings of the target user
     * @param userRatings - all user ratings
     * @return a map of halls with similarity scores
     */
    private Map<Hall, Double> calculateItemSimilarity(List<UserHallRatings> targetUserRatings, List<UserHallRatings> userRatings) {
        Map<Hall, Double> similarityMap = new HashMap<>(); // <-- Similarity map for halls

        // Step 1: Create a set of halls that the target user hasn't rated yet
        Set<Hall> unratedHalls = userRatings.stream() // <-- Stream filtering to get unrated halls
                .map(UserHallRatings::getHall)
                .filter(hall -> targetUserRatings.stream().noneMatch(rating -> rating.getHall().equals(hall)))
                .collect(Collectors.toSet());

        // Step 2: For each unrated hall, calculate the similarity score based on other halls
        for (Hall unratedHall : unratedHalls) {
            double similarityScore = 0.0; // <-- Initialize similarity score

            for (UserHallRatings targetRating : targetUserRatings) {
                Hall ratedHall = targetRating.getHall();

                // Find the ratings for the current unrated hall and rated hall
                List<UserHallRatings> ratedHallRatings = userRatings.stream()
                        .filter(r -> r.getHall().equals(ratedHall))
                        .collect(Collectors.toList());

                List<UserHallRatings> unratedHallRatings = userRatings.stream()
                        .filter(r -> r.getHall().equals(unratedHall))
                        .collect(Collectors.toList());

                // Calculate cosine similarity between rated and unrated halls
                similarityScore += calculateCosineSimilarity(ratedHallRatings, unratedHallRatings); // <-- Call to cosine similarity function
            }

            similarityMap.put(unratedHall, similarityScore); // <-- Store similarity score
        }

        return similarityMap;
    }

    /**
     * Calculate cosine similarity between two sets of hall ratings
     * @param ratedHallRatings - ratings for a hall the target user rated
     * @param unratedHallRatings - ratings for a hall the target user hasn't rated
     * @return similarity score
     */
    private double calculateCosineSimilarity(List<UserHallRatings> ratedHallRatings, List<UserHallRatings> unratedHallRatings) {
        // Convert list to maps for easy access
        Map<Long, Double> ratedMap = ratedHallRatings.stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), UserHallRatings::getRating));

        Map<Long, Double> unratedMap = unratedHallRatings.stream()
                .collect(Collectors.toMap(r -> r.getUser().getId(), UserHallRatings::getRating));

        // Find common users who rated both halls
        Set<Long> commonUsers = new HashSet<>(ratedMap.keySet());
        commonUsers.retainAll(unratedMap.keySet());

        if (commonUsers.isEmpty()) {
            return 0.0; // No common users, similarity is zero
        }

        // Calculate cosine similarity
        double dotProduct = 0.0, normRated = 0.0, normUnrated = 0.0;

        for (Long userId : commonUsers) {
            double ratedRating = ratedMap.get(userId);
            double unratedRating = unratedMap.get(userId);

            dotProduct += ratedRating * unratedRating;
            normRated += Math.pow(ratedRating, 2);
            normUnrated += Math.pow(unratedRating, 2);
        }

        // Final similarity score
        return dotProduct / (Math.sqrt(normRated) * Math.sqrt(normUnrated));
    }

    /**
     * Fallback recommendations based on popularity or recency
     * @return List of fallback halls
     */
    private List<Hall> fallbackHallRecommendations() {
        // Get the most popular halls based on average ratings
        List<Hall> mostPopularHalls = hallRepository.findAll().stream()
                .sorted((hall1, hall2) -> Double.compare(calculateAverageRating(hall2), calculateAverageRating(hall1))) // <-- Sort by popularity
                .limit(5) // <-- Limit to top 5
                .collect(Collectors.toList());

        // Get recent halls if popularity list isn't enough
        List<Hall> recentHalls = hallRepository.findAll().stream()
                .sorted(Comparator.comparing(Hall::getCreatedDate).reversed()) // <-- Sort by recency
                .limit(5) // <-- Limit to top 5
                .collect(Collectors.toList());

        // Combine both lists
        Set<Hall> recommendations = new LinkedHashSet<>(mostPopularHalls);
        recommendations.addAll(recentHalls);

        return new ArrayList<>(recommendations).subList(0, Math.min(5, recommendations.size())); // <-- Ensure a maximum of 5 recommendations
    }

    /**
     * Calculate the average rating for a hall
     * @param hall - the hall to calculate
     * @return the average rating
     */
    private double calculateAverageRating(Hall hall) {
        // Calculate average rating for a hall
        List<UserHallRatings> ratings = userHallRatingsRepository.findByHall(hall);
        if (ratings.isEmpty()) {
            return 0.0; // No ratings
        }
        return ratings.stream().mapToDouble(UserHallRatings::getRating).average().orElse(0.0); // <-- Calculate average
    }

    public GeneralResponse rateHall(RatingDTO rating) throws UserNotFoundException {
        User user = userRepository.findById(rating.getUserId()).orElseThrow(
                () -> new UserNotFoundException("User not found with id: " + rating.getUserId()));

        Hall hall = hallRepository.findById(rating.getHallId()).orElseThrow(
                () -> new UserNotFoundException("Hall not found with id: " + rating.getHallId()));

        UserHallRatings existingRating = userHallRatingsRepository.findByUserIdAndHallId(user.getId(), hall.getId());

        if (existingRating != null) {
            existingRating.setRating(rating.getRating());
            userHallRatingsRepository.save(existingRating);
            System.out.println("Rating updated successfully");
        } else {
            UserHallRatings newRating = UserHallRatings.builder()
                    .user(user)
                    .hall(hall)
                    .rating(rating.getRating())
                    .comment(rating.getComment())
                    .build();
            userHallRatingsRepository.save(newRating);
        }

        // Recalculate the average rating
        Double averageRating = userHallRatingsRepository.findAverageRatingByHallId(hall.getId());
        if (averageRating != null) {
            hall.setAverageRating(averageRating);
            hallRepository.save(hall);
        }

        return new GeneralResponse("Rating saved successfully");
    }





    public UserHallRatings getRating(Long userid, Long hallId) throws UserNotFoundException {
        User user = userRepository.findById(userid).orElseThrow(
                () -> new UserNotFoundException("User not found w "));

        Hall hall = hallRepository.findById(hallId).orElseThrow(
                () -> new UserNotFoundException("Hall not found"));
        return userHallRatingsRepository.findByUserIdAndHallId(user.getId(), hall.getId());
    }
}
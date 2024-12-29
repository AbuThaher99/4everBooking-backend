package org.example.GraduationProject.Core.Servecies;

import org.example.GraduationProject.Common.DTOs.RatingDTO;
import org.example.GraduationProject.Common.Entities.*;

import org.example.GraduationProject.Common.Responses.GeneralResponse;
import org.example.GraduationProject.Core.Repsitories.*;
import org.example.GraduationProject.WebApi.Exceptions.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private reservationsRepository reservationsRepository;

    @Autowired
    private UserHallRecommendationRepository userHallRecommendationRepository;


    public List<Hall> recommendHalls(User user, int size) {
        // Fetch all halls
        List<Hall> allHalls = hallRepository.findAllHalls();
        System.out.println("Total halls in repository: " + allHalls.size());

        // Fetch all user-hall ratings
        List<UserHallRatings> userRatings = userHallRatingsRepository.findAll();

        // Fetch ratings for the target user
        List<UserHallRatings> targetUserRatings = userRatings.stream()
                .filter(rating -> rating.getUser().equals(user))
                .collect(Collectors.toList());

        // If no ratings exist, return fallback recommendations
        if (targetUserRatings.isEmpty()) {
            System.out.println("Target user has no ratings. Returning fallback recommendations.");
            return fallbackHallRecommendations(allHalls, size);
        }

        // Step 1: Calculate similarity between halls based on ratings
        Map<Hall, Double> hallSimilarities = calculateItemSimilarity(targetUserRatings, userRatings, allHalls);

        // Step 2: Return halls based on similarity scores
        List<Hall> recommendedHalls = hallSimilarities.entrySet().stream()
                .sorted(Map.Entry.<Hall, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("Total halls fetched for recommendations: " + recommendedHalls.size());

        // Respect size limit only if size is not Integer.MAX_VALUE
        if (size != Integer.MAX_VALUE) {
            return recommendedHalls.stream().limit(size).collect(Collectors.toList());
        }

        return recommendedHalls; // Return all halls if size is Integer.MAX_VALUE
    }




    /**
     * Calculate similarity between halls based on user ratings.
     * This uses cosine similarity between item (hall) ratings.
     * @param targetUserRatings - the ratings of the target user
     * @param userRatings - all user ratings
     * @return a map of halls with similarity scores
     */
    private Map<Hall, Double> calculateItemSimilarity(List<UserHallRatings> targetUserRatings, List<UserHallRatings> userRatings, List<Hall> allHalls) {
        Map<Hall, Double> similarityMap = new HashMap<>();

        System.out.println("Total halls for similarity calculation: " + allHalls.size());

        for (Hall hall : allHalls) {
            double similarityScore = 0.0;

            for (UserHallRatings targetRating : targetUserRatings) {
                Hall ratedHall = targetRating.getHall();

                // Find ratings for the current hall
                List<UserHallRatings> ratedHallRatings = userRatings.stream()
                        .filter(r -> r.getHall().equals(ratedHall))
                        .collect(Collectors.toList());

                List<UserHallRatings> hallRatings = userRatings.stream()
                        .filter(r -> r.getHall().equals(hall))
                        .collect(Collectors.toList());

                // Calculate similarity
                similarityScore += calculateCosineSimilarity(ratedHallRatings, hallRatings);
            }

            similarityMap.put(hall, similarityScore);
        }

        System.out.println("Total halls added to similarity map: " + similarityMap.size());
        return similarityMap;
    }





    /**
     * Calculate cosine similarity between two sets of hall ratings
     * @param ratedHallRatings - ratings for a hall the target user rated
     * @param unratedHallRatings - ratings for a hall the target user hasn't rated
     * @return similarity score
     */
    private double calculateCosineSimilarity(List<UserHallRatings> ratedHallRatings, List<UserHallRatings> unratedHallRatings) {
        Map<Long, Double> ratedMap = ratedHallRatings.stream()
                .collect(Collectors.toMap(
                        r -> r.getUser().getId(),
                        UserHallRatings::getRating,
                        (existing, replacement) -> existing // Handle duplicates
                ));

        Map<Long, Double> unratedMap = unratedHallRatings.stream()
                .collect(Collectors.toMap(
                        r -> r.getUser().getId(),
                        UserHallRatings::getRating,
                        (existing, replacement) -> existing // Handle duplicates
                ));

        Set<Long> commonUsers = new HashSet<>(ratedMap.keySet());
        commonUsers.retainAll(unratedMap.keySet());

        if (commonUsers.isEmpty()) {
            return 0.0; // No common users, similarity is zero
        }

        double dotProduct = 0.0, normRated = 0.0, normUnrated = 0.0;

        for (Long userId : commonUsers) {
            double ratedRating = ratedMap.get(userId);
            double unratedRating = unratedMap.get(userId);

            dotProduct += ratedRating * unratedRating;
            normRated += Math.pow(ratedRating, 2);
            normUnrated += Math.pow(unratedRating, 2);
        }

        return dotProduct / (Math.sqrt(normRated) * Math.sqrt(normUnrated));
    }


    /**
     * Fallback recommendations based on popularity or recency
     * @return List of fallback halls
     */
    private List<Hall> fallbackHallRecommendations(List<Hall> allHalls, int size) {
        System.out.println("Total halls fetched from repository: " + allHalls.size());

        List<Hall> mostPopularHalls = allHalls.stream()
                .sorted((hall1, hall2) -> Double.compare(calculateAverageRating(hall2), calculateAverageRating(hall1)))
                .collect(Collectors.toList());

        System.out.println("Total halls in fallback after sorting by popularity: " + mostPopularHalls.size());

        if (size != Integer.MAX_VALUE) {
            return mostPopularHalls.stream().limit(size).collect(Collectors.toList());
        }

        return mostPopularHalls;
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
        // Find the user by ID
        User user = userRepository.findById(rating.getUserId()).orElseThrow(
                () -> new UserNotFoundException("User not found with id: " + rating.getUserId()));

        // Check if the user is a customer
        if (user.getCustomer() == null) {
            throw new UserNotFoundException("User is not a customer");
        }

        // Find the reservation by ID
        Reservations reservation = reservationsRepository.findById(rating.getReservationId()).orElseThrow(
                () -> new UserNotFoundException("Reservation not found with id: " + rating.getReservationId()));

        // Ensure the reservation belongs to the user and hall
        if (!reservation.getCustomer().equals(user.getCustomer()) ||
                reservation.isRated()) {
            return new GeneralResponse("Reservation already rated or does not belong to the user");
        }

        // Find the hall associated with the reservation
        Hall hall = reservation.getHall();

        // Create a new rating entry for the user and hall
        UserHallRatings newRating = UserHallRatings.builder()
                .user(user)
                .hall(hall)
                .rating(rating.getRating())
                .comment(rating.getComment())
                .build();

        // Save the new rating in the repository
        userHallRatingsRepository.save(newRating);

        // Mark the reservation as rated
        reservation.setRated(true);
        reservationsRepository.save(reservation);

        // Recalculate the average rating for the hall
        Double averageRating = userHallRatingsRepository.findAverageRatingByHallId(hall.getId());
        if (averageRating != null) {
            hall.setAverageRating(averageRating);
            hallRepository.save(hall);
        }

        return new GeneralResponse("Rating saved successfully and reservation marked as rated");
    }

    public UserHallRatings getRating(Long userid, Long hallId) throws UserNotFoundException {
        User user = userRepository.findById(userid).orElseThrow(
                () -> new UserNotFoundException("User not found w "));

        Hall hall = hallRepository.findById(hallId).orElseThrow(
                () -> new UserNotFoundException("Hall not found"));
        return userHallRatingsRepository.findByUserIdAndHallId(user.getId(), hall.getId());
    }

    private double calculateHallScore(Hall hall, User user) {
        List<UserHallRatings> allRatings = userHallRatingsRepository.findAll();

        List<UserHallRatings> userRatings = allRatings.stream()
                .filter(rating -> rating.getUser().equals(user))
                .collect(Collectors.toList());

        if (userRatings.isEmpty()) {
            return 0.0;
        }

        List<UserHallRatings> hallRatings = allRatings.stream()
                .filter(rating -> rating.getHall().equals(hall))
                .collect(Collectors.toList());

        if (hallRatings.isEmpty()) {
            return 0.0;
        }

        double similarityScore = 0.0;
        for (UserHallRatings userRating : userRatings) {
            Hall ratedHall = userRating.getHall();

            List<UserHallRatings> ratedHallRatings = allRatings.stream()
                    .filter(r -> r.getHall().equals(ratedHall))
                    .collect(Collectors.toList());

            similarityScore += calculateCosineSimilarity(ratedHallRatings, hallRatings);
        }

        return similarityScore;
    }

    @Transactional
    public void saveRecommendations(User user, int size) {
        System.out.println("Saving recommendations for user: " + user.getUsername());

        // Fetch recommendations
        List<Hall> recommendedHalls = recommendHalls(user, size);
        System.out.println("Total halls fetched for recommendations: " + recommendedHalls.size());

        // Delete old recommendations
        userHallRecommendationRepository.deleteByUser(user);
        System.out.println("Old recommendations deleted.");

        // Save new recommendations
        List<UserHallRecommendation> recommendations = recommendedHalls.stream()
                .map(hall -> new UserHallRecommendation(user, hall, calculateHallScore(hall, user)))
                .collect(Collectors.toList());

        System.out.println("Total recommendations to save: " + recommendations.size());
        userHallRecommendationRepository.saveAll(recommendations);
        System.out.println("New recommendations saved.");
    }


}
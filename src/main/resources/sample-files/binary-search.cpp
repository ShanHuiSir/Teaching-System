/**
 * Binary Search Implementation
 *
 * A classic divide-and-conquer algorithm that finds the position of a target
 * value within a sorted array. Time complexity: O(log n), Space: O(1).
 *
 * This implementation includes both iterative and recursive versions with
 * proper boundary checking and error handling.
 */

#include <iostream>
#include <vector>
#include <stdexcept>

/**
 * Iterative binary search.
 * @param arr  A sorted vector of integers (ascending order).
 * @param target  The value to search for.
 * @return  The index of target in arr, or -1 if not found.
 */
int binarySearchIterative(const std::vector<int>& arr, int target) {
    if (arr.empty()) return -1;
    int left = 0, right = static_cast<int>(arr.size()) - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return -1;
}

/**
 * Recursive binary search helper.
 */
int binarySearchRecursiveHelper(const std::vector<int>& arr, int target,
                                 int left, int right) {
    if (left > right) return -1;
    int mid = left + (right - left) / 2;
    if (arr[mid] == target) return mid;
    if (arr[mid] < target)
        return binarySearchRecursiveHelper(arr, target, mid + 1, right);
    else
        return binarySearchRecursiveHelper(arr, target, left, mid - 1);
}

/**
 * Recursive binary search (public interface).
 */
int binarySearchRecursive(const std::vector<int>& arr, int target) {
    if (arr.empty()) return -1;
    return binarySearchRecursiveHelper(arr, target, 0,
                                       static_cast<int>(arr.size()) - 1);
}

// Test harness
int main() {
    std::vector<int> data = {2, 5, 8, 12, 16, 23, 38, 45, 56, 67, 78, 89};
    std::cout << "=== Binary Search Test ===" << std::endl;
    std::cout << "Search 23: " << binarySearchIterative(data, 23) << " (expected 5)" << std::endl;
    std::cout << "Search 100: " << binarySearchRecursive(data, 100) << " (expected -1)" << std::endl;
    std::cout << "Search 2: " << binarySearchIterative(data, 2) << " (expected 0)" << std::endl;
    std::cout << "Search 89: " << binarySearchRecursive(data, 89) << " (expected 11)" << std::endl;
    return 0;
}

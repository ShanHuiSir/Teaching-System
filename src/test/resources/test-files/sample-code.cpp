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
 *
 * @param arr  A sorted vector of integers (ascending order).
 * @param target  The value to search for.
 * @return  The index of target in arr, or -1 if not found.
 */
int binarySearchIterative(const std::vector<int>& arr, int target) {
    if (arr.empty()) {
        return -1;
    }

    int left = 0;
    int right = static_cast<int>(arr.size()) - 1;

    while (left <= right) {
        // Avoid overflow: mid = left + (right - left) / 2
        int mid = left + (right - left) / 2;

        if (arr[mid] == target) {
            return mid;
        } else if (arr[mid] < target) {
            left = mid + 1;
        } else {
            right = mid - 1;
        }
    }

    return -1;  // Not found
}

/**
 * Recursive helper for binary search.
 */
int binarySearchRecursiveHelper(const std::vector<int>& arr, int target,
                                 int left, int right) {
    if (left > right) {
        return -1;
    }

    int mid = left + (right - left) / 2;

    if (arr[mid] == target) {
        return mid;
    } else if (arr[mid] < target) {
        return binarySearchRecursiveHelper(arr, target, mid + 1, right);
    } else {
        return binarySearchRecursiveHelper(arr, target, left, mid - 1);
    }
}

/**
 * Recursive binary search (public interface).
 *
 * @param arr  A sorted vector of integers.
 * @param target  The value to search for.
 * @return  Index of target, or -1 if not found.
 */
int binarySearchRecursive(const std::vector<int>& arr, int target) {
    if (arr.empty()) {
        return -1;
    }
    return binarySearchRecursiveHelper(arr, target, 0,
                                       static_cast<int>(arr.size()) - 1);
}

// ── Simple test harness ──────────────────────────────────────────────

int main() {
    std::vector<int> data = {2, 5, 8, 12, 16, 23, 38, 45, 56, 67, 78, 89};

    std::cout << "=== Binary Search Test ===" << std::endl;

    // Test case 1: element exists
    int idx1 = binarySearchIterative(data, 23);
    std::cout << "Iterative search for 23: index " << idx1
              << " (expected 5)" << std::endl;

    // Test case 2: element does not exist
    int idx2 = binarySearchRecursive(data, 100);
    std::cout << "Recursive search for 100: index " << idx2
              << " (expected -1)" << std::endl;

    // Test case 3: boundary - first element
    int idx3 = binarySearchIterative(data, 2);
    std::cout << "Iterative search for 2: index " << idx3
              << " (expected 0)" << std::endl;

    // Test case 4: boundary - last element
    int idx4 = binarySearchRecursive(data, 89);
    std::cout << "Recursive search for 89: index " << idx4
              << " (expected 11)" << std::endl;

    // Test case 5: empty vector
    std::vector<int> empty;
    int idx5 = binarySearchIterative(empty, 10);
    std::cout << "Search in empty vector: index " << idx5
              << " (expected -1)" << std::endl;

    return 0;
}

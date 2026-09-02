package edu.review.moviesappreview.domain.repository

/**
 * Marker Interface that serves as the base contract for all data-retrieval
 * components (e.g., Remote, Local, or Cache).
 * @param T The type of data handled by this source.
 */
interface DataSource<out T>

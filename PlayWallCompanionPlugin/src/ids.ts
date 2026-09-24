/**
 * Companion action/feedback ids, defined once so a rename only needs to happen here — everywhere
 * else (the `ModuleSchema` type, the definitions themselves, `checkFeedbacks()` calls) references
 * these constants instead of repeating the string literal.
 */
export const PAD_PLAY_STOP_ACTION_ID = 'pad_play_stop'
export const PAD_CURRENT_COLOR_FEEDBACK_ID = 'pad_current_color'
export const PAGE_NAVIGATE_ACTION_ID = 'page_navigate'
export const PAGE_CURRENT_COLOR_FEEDBACK_ID = 'page_current_color'

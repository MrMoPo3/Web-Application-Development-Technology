import { configureStore } from '@reduxjs/toolkit';
import authReducer from './authSlice.js';
import pollsReducer from './pollsSlice.js';
import { loadState, saveState } from './storage.js';

const persistedState = loadState();
const preloadedState = persistedState
  ? {
      ...persistedState,
      polls: persistedState.polls
        ? {
            ...persistedState.polls,
            status: 'idle',
            error: null,
          }
        : undefined,
    }
  : undefined;

export const store = configureStore({
  reducer: {
    auth: authReducer,
    polls: pollsReducer,
  },
  preloadedState,
});

store.subscribe(() => {
  const { status, error, ...persistablePolls } = store.getState().polls;

  saveState({
    auth: store.getState().auth,
    polls: persistablePolls,
  });
});

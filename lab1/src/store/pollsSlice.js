import { createAsyncThunk, createSlice, nanoid } from '@reduxjs/toolkit';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

function normalizePoll(apiPoll) {
  return {
    id: String(apiPoll.id),
    title: apiPoll.title,
    description: apiPoll.description || 'Без опису',
    options: (apiPoll.choices ?? []).map((choice) => ({
      id: String(choice.id),
      text: choice.text,
      votes: choice.votes_count ?? 0,
    })),
    createdAt: apiPoll.created_at?.slice(0, 10) ?? '',
  };
}

export const fetchRemotePolls = createAsyncThunk('polls/fetchRemotePolls', async () => {
  const response = await globalThis.fetch(`${API_BASE_URL}/polls/`);

  if (!response.ok) {
    throw new Error('Не вдалося завантажити опитування з API.');
  }

  const data = await response.json();
  const results = Array.isArray(data) ? data : data.results ?? [];
  return results.map(normalizePoll);
});

const initialState = {
  status: 'idle',
  error: null,
  items: [
    {
      id: 'starter-poll',
      title: 'Який формат опитувань найзручніший для навчального проєкту?',
      description: 'Початкове демонстраційне опитування зі статистикою голосів.',
      options: [
        { id: 'single', text: 'Одне питання з варіантами', votes: 8 },
        { id: 'scale', text: 'Шкала оцінювання', votes: 5 },
        { id: 'open', text: 'Відкрита відповідь', votes: 3 },
      ],
      createdAt: '2026-05-18',
    },
  ],
};

const pollsSlice = createSlice({
  name: 'polls',
  initialState,
  reducers: {
    addPoll: {
      reducer(state, action) {
        state.items.unshift(action.payload);
      },
      prepare({ title, description, options }) {
        return {
          payload: {
            id: nanoid(),
            title,
            description,
            options: options.map((text) => ({
              id: nanoid(),
              text,
              votes: 0,
            })),
            createdAt: new Date().toISOString().slice(0, 10),
          },
        };
      },
    },
    addOption(state, action) {
      const poll = state.items.find((item) => item.id === action.payload.pollId);

      if (poll) {
        poll.options.push({
          id: nanoid(),
          text: action.payload.text,
          votes: 0,
        });
      }
    },
    vote(state, action) {
      const poll = state.items.find((item) => item.id === action.payload.pollId);
      const option = poll?.options.find((item) => item.id === action.payload.optionId);

      if (option) {
        option.votes += 1;
      }
    },
    removePoll(state, action) {
      state.items = state.items.filter((item) => item.id !== action.payload);
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchRemotePolls.pending, (state) => {
        state.status = 'loading';
        state.error = null;
      })
      .addCase(fetchRemotePolls.fulfilled, (state, action) => {
        state.status = 'succeeded';
        state.error = null;

        if (action.payload.length > 0) {
          state.items = action.payload;
        }
      })
      .addCase(fetchRemotePolls.rejected, (state, action) => {
        state.status = 'failed';
        state.error = action.error.message ?? 'API тимчасово недоступний.';
      });
  },
});

export const { addPoll, addOption, vote, removePoll } = pollsSlice.actions;
export default pollsSlice.reducer;

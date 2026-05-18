// About App
pm.test('Status is 200', () => pm.response.to.have.status(200));
pm.test('Response has app name', () => pm.expect(pm.response.json().name).to.eql('PollCraft'));

// Register User prerequest
pm.collectionVariables.set('email', `test${Date.now()}@example.com`);

// Register User tests
pm.test('Status is 201', () => pm.response.to.have.status(201));
pm.test('Token returned', () => pm.expect(pm.response.json().token).to.be.a('string'));
pm.collectionVariables.set('token', pm.response.json().token);

// Login User
pm.test('Status is 200', () => pm.response.to.have.status(200));
pm.test('Token returned', () => pm.expect(pm.response.json().token).to.be.a('string'));
pm.collectionVariables.set('token', pm.response.json().token);

// Get Profile
pm.test('Status is 200', () => pm.response.to.have.status(200));
pm.test('Profile has email', () => pm.expect(pm.response.json().email).to.include('@example.com'));

// Create Poll
pm.test('Status is 201', () => pm.response.to.have.status(201));
const body = pm.response.json();
pm.collectionVariables.set('poll_id', body.id);
pm.collectionVariables.set('choice_id', body.choices[0].id);

// Vote
pm.test('Vote accepted or already exists', () => pm.expect([201, 400]).to.include(pm.response.code));

// Poll Stats
pm.test('Status is 200', () => pm.response.to.have.status(200));
pm.test('Stats contains total votes', () => pm.expect(pm.response.json()).to.have.property('total_votes'));

// ReDoc
pm.test('Status is 200', () => pm.response.to.have.status(200));

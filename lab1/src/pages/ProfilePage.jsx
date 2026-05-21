import { useSelector } from 'react-redux';
import PageHeader from '../components/PageHeader.jsx';
import RequireUserNotice from '../components/RequireUserNotice.jsx';
import { selectCurrentUser } from '../utils/selectors.js';

export default function ProfilePage() {
  const user = useSelector(selectCurrentUser);

  if (!user) {
    return (
      <section className="container page-narrow">
        <RequireUserNotice />
      </section>
    );
  }

  const rows = [
    ["Ім'я", user.name],
    ['Email', user.email],
    ['Стать', user.gender],
    ['Дата народження', user.birthDate],
  ];

  return (
    <section className="container page-narrow">
      <PageHeader eyebrow="Профіль" title="Дані користувача">

      </PageHeader>

      <div className="table-responsive profile-table">
        <table className="table align-middle">
          <tbody>
            {rows.map(([label, value]) => (
              <tr key={label}>
                <th scope="row">{label}</th>
                <td>{value}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
